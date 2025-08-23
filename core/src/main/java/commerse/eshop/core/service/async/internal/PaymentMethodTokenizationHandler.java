package commerse.eshop.core.service.async.internal;


import commerse.eshop.core.events.PaymentMethodCreatedEvent;
import commerse.eshop.core.model.entity.CustomerPaymentMethod;
import commerse.eshop.core.model.entity.enums.TokenStatus;
import commerse.eshop.core.repository.CustomerPaymentMethodRepo;
import commerse.eshop.core.service.async.external.ProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class PaymentMethodTokenizationHandler {

    private final ProviderClient providerClient;
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;

    @Autowired
    public PaymentMethodTokenizationHandler(ProviderClient providerClient, CustomerPaymentMethodRepo customerPaymentMethodRepo){
        this.providerClient = providerClient;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentMethodCreatedEvent paymentMethodCreatedEvent){
        CustomerPaymentMethod paymentMethod = customerPaymentMethodRepo.getReferenceById(paymentMethodCreatedEvent.paymentId());

        // idempotency guard: process only if its pending.
        if(paymentMethod.getTokenStatus() != TokenStatus.PENDING){
            log.info("Skip tokenization, status={}", paymentMethod.getTokenStatus());
            return; // Early return
        }

        try{
            String token = providerClient.fetchPaymentToken(paymentMethod.getProvider(), paymentMethod);
            paymentMethod.setProviderPaymentMethodToken(token);
            paymentMethod.setTokenStatus(TokenStatus.ACTIVE);
            log.info("Tokenized the paymentMethod id={} -> Active", paymentMethod.getCustomerPaymentId());
        } catch (Exception ex){
            paymentMethod.setTokenStatus(TokenStatus.FAILED);
            log.warn("Tokenization failed for id={}: {}", paymentMethodCreatedEvent.paymentId(), ex.getMessage());
        }

        customerPaymentMethodRepo.saveAndFlush(paymentMethod);
    }


}
