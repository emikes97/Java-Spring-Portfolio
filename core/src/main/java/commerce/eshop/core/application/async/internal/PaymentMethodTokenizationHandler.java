package commerce.eshop.core.application.async.internal;


import commerce.eshop.core.application.events.customer.PaymentMethodCreatedEvent;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.TokenStatus;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import commerce.eshop.core.application.async.external.ProviderClient;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.NoSuchElementException;

@Component
@Slf4j
public class PaymentMethodTokenizationHandler {

    private final ProviderClient providerClient;
    private final CustomerPaymentMethodRepo customerPaymentMethodRepo;
    private final CentralAudit centralAudit;

    @Autowired
    public PaymentMethodTokenizationHandler(ProviderClient providerClient,
                                            CustomerPaymentMethodRepo customerPaymentMethodRepo,
                                            CentralAudit centralAudit){
        this.providerClient = providerClient;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.centralAudit = centralAudit;
    }

    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentMethodCreatedEvent paymentMethodCreatedEvent){

        final CustomerPaymentMethod paymentMethod;

        try {
            paymentMethod = customerPaymentMethodRepo.findByCustomer_CustomerIdAndCustomerPaymentId(
                    paymentMethodCreatedEvent.customerId(), paymentMethodCreatedEvent.paymentId()).orElseThrow(
                    () -> new NoSuchElementException("Customer_id = " + paymentMethodCreatedEvent.customerId() + " and " +
                            "payment_id = " + paymentMethodCreatedEvent.paymentId() + " don't match." )
            );
        } catch (NoSuchElementException e){
            throw centralAudit.audit(e, paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC, AuditingStatus.WARNING, e.toString());
        }

        // Process only when it's freshly created (PENDING). Otherwise skip.
        if(paymentMethod.getTokenStatus() != TokenStatus.PENDING){
            log.info("Skip tokenization, status={}", paymentMethod.getTokenStatus());
            centralAudit.info(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Tokenization skipped");
            return; // Early return
        }

        // Try to become the processor (optimistic lock)
        try {
            paymentMethod.setTokenStatus(TokenStatus.PROCESSING);
            customerPaymentMethodRepo.flush(); // will throw if another worker updated the row (version changed)
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException race) {
            log.info("Race lost; another worker started tokenization id={}", paymentMethodCreatedEvent.paymentId());
            centralAudit.warn(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.WARNING, "SKIPPED_RACE_LOST id=" + paymentMethodCreatedEvent.paymentId());
            return;
        }

        // Winner does the external call
        try {
            String token = providerClient.fetchPaymentToken(paymentMethod.getProvider(), paymentMethod);
            paymentMethod.setProviderPaymentMethodToken(token);          // never log the token
            paymentMethod.setTokenStatus(TokenStatus.ACTIVE);
            customerPaymentMethodRepo.saveAndFlush(paymentMethod);
            centralAudit.info(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.SUCCESSFUL, "TOKENIZED id=" + paymentMethodCreatedEvent.paymentId());
        } catch (Exception ex) {
            paymentMethod.setTokenStatus(TokenStatus.FAILED);
            customerPaymentMethodRepo.saveAndFlush(paymentMethod);
            log.warn("Tokenization failed for id={}: {}", paymentMethodCreatedEvent.paymentId(), ex.getMessage());
            centralAudit.info(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.FAILED, "TOKENIZATION_FAILED id=" + paymentMethodCreatedEvent.paymentId());
        }
    }
}
