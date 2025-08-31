package commerse.eshop.core.service.async.internal;

import commerse.eshop.core.events.PaymentExecutionRequestEvent;
import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.model.entity.enums.TransactionStatus;
import commerse.eshop.core.repository.TransactionRepo;
import commerse.eshop.core.service.async.external.PaymentProviderClient;
import commerse.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessor {

    private final PaymentProviderClient paymentProviderClient;
    private final TransactionRepo transactionRepo;

    @Autowired
    public PaymentProcessor(PaymentProviderClient paymentProviderClient, TransactionRepo transactionRepo){
        this.paymentProviderClient = paymentProviderClient;
        this.transactionRepo = transactionRepo;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPayment(PaymentExecutionRequestEvent paymentExecutionRequestEvent){
        Transaction tr = transactionRepo.findByIdempotencyKey(paymentExecutionRequestEvent.idemKey()).orElseThrow(
                () -> new IllegalArgumentException("No transaction with idemKey = " + paymentExecutionRequestEvent.idemKey() + " exists.")
        );

        if (tr.getStatus() == TransactionStatus.SUCCESSFUL || tr.getStatus() == TransactionStatus.FAILED) return;

        Map<String, Object> snap = Optional.ofNullable(paymentExecutionRequestEvent.snapshot()).orElse(Map.of());
        ProviderChargeResult providerChargeResult;

        if(paymentExecutionRequestEvent.orderId().equals(tr.getOrder().getOrderId())){
            try{
                Map<String, Object> outcome;
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
                }

            } catch (DataIntegrityViolationException err){
                throw new IllegalStateException(err);
            }
        }
    }

    private Integer toInt(Object o){
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }

}
