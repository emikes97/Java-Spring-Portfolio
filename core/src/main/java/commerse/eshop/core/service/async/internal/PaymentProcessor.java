package commerse.eshop.core.service.async.internal;

import commerse.eshop.core.events.PaymentExecutionRequestEvent;
import commerse.eshop.core.events.PaymentSucceededOrFailed;
import commerse.eshop.core.model.entity.Customer;
import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.enums.OrderStatus;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import commerse.eshop.core.repository.OrderRepo;
import commerse.eshop.core.repository.TransactionRepo;
import commerse.eshop.core.service.async.external.PaymentProviderClient;
import commerse.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessor {

    private final PaymentProviderClient paymentProviderClient;
    private final TransactionRepo transactionRepo;
    private final OrderRepo orderRepo;
    private final ApplicationEventPublisher publisher;

    @Autowired
    public PaymentProcessor(PaymentProviderClient paymentProviderClient, TransactionRepo transactionRepo, OrderRepo orderRepo, ApplicationEventPublisher publisher){
        this.paymentProviderClient = paymentProviderClient;
        this.transactionRepo = transactionRepo;
        this.orderRepo = orderRepo;
        this.publisher = publisher;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPayment(PaymentExecutionRequestEvent paymentExecutionRequestEvent){
        Transaction tr = transactionRepo.findByIdempotencyKeyForUpdate(paymentExecutionRequestEvent.idemKey())
                .orElseThrow(() -> new IllegalArgumentException("No transaction with idemKey = " + paymentExecutionRequestEvent.idemKey()));

        if(!paymentExecutionRequestEvent.orderId().equals(tr.getOrder().getOrderId())) return;
        if (tr.getStatus() == TransactionStatus.SUCCESSFUL || tr.getStatus() == TransactionStatus.FAILED) return;

        Map<String, Object> snap = Optional.ofNullable(paymentExecutionRequestEvent.snapshot()).orElse(Map.of());
        ProviderChargeResult providerChargeResult = null;

        try{
            switch (paymentExecutionRequestEvent.methodType()){
                case "USE_SAVED_METHOD" -> {
                    Object idObjPaymentMethod = snap.get("customerPaymentMethodId");
                    UUID customerPaymentMethodId = (idObjPaymentMethod instanceof UUID u) ? u : UUID.fromString(String.valueOf(idObjPaymentMethod));
                    providerChargeResult = paymentProviderClient.chargeWithSavedMethod(tr, customerPaymentMethodId);
                }
                case "USE_NEW_CARD" -> {
                    String brand = (String) snap.get("brand");
                    String panMasked = (String) snap.get("panMasked");
                    Integer expMonth = toInt(snap.get("expMonth"));
                    Integer expYear  = toInt(snap.get("expYear"));
                    String holder    = (String) snap.get("holderName");
                    providerChargeResult = paymentProviderClient.tokenizeAndCharge(tr, brand, panMasked, expMonth, expYear, holder);
                }

                default -> throw new IllegalArgumentException("Unsupported payment method: " + paymentExecutionRequestEvent.methodType());
            }
        } catch (DataIntegrityViolationException err){
            showErrorMessage(providerChargeResult, err, tr);
            return;
        } catch (Exception ex) {
            showErrorMessage(providerChargeResult, ex, tr);
            return;
        }

        if(providerChargeResult.successful()){
            tr.setStatus(TransactionStatus.SUCCESSFUL);
            tr.setCompletedAt(OffsetDateTime.now());
            log.info("Transaction: " + tr.getTransactionId() + "was completed");
            transactionRepo.save(tr);
            publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAID, OffsetDateTime.now(), tr.getOrder().getOrderId()));
        } else {
            tr.setStatus(TransactionStatus.FAILED);
            tr.setCompletedAt(OffsetDateTime.now());
            log.info("Transaction: " + tr.getTransactionId() + "failed");
            orderRepo.restoreProductStockFromOrder(tr.getOrder().getOrderId());
            transactionRepo.save(tr);
            publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAYMENT_FAILED, OffsetDateTime.now(), tr.getOrder().getOrderId()));
        }
    }

    private void showErrorMessage(ProviderChargeResult providerChargeResult, Exception exception, Transaction tr){
        if (providerChargeResult == null) {
            // provider wasn't called or failed before returning any result
            providerChargeResult = new ProviderChargeResult(null, false, "PROVIDER_ERROR", exception.getMessage());
        }
        log.debug("[ERROR]: " + providerChargeResult.errorCode() + " " + providerChargeResult.errorMessage() + " -- " + exception);
        log.info("[ERROR]: " + providerChargeResult.errorCode() + " for transaction " + tr.getTransactionId());
        tr.setStatus(TransactionStatus.FAILED);
        tr.setCompletedAt(OffsetDateTime.now());
        orderRepo.restoreProductStockFromOrder(tr.getOrder().getOrderId());
        transactionRepo.save(tr);
        publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAYMENT_FAILED, OffsetDateTime.now(), tr.getOrder().getOrderId()));
    }

    private Integer toInt(Object o){
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }
}
