package commerce.eshop.core.service.async.internal;


import commerce.eshop.core.events.PaymentMethodCreatedEvent;
import commerce.eshop.core.model.entity.CustomerPaymentMethod;
import commerce.eshop.core.util.constants.EndpointsNameMethods;
import commerce.eshop.core.util.enums.AuditingStatus;
import commerce.eshop.core.util.enums.TokenStatus;
import commerce.eshop.core.repository.CustomerPaymentMethodRepo;
import commerce.eshop.core.service.AuditingService;
import commerce.eshop.core.service.async.external.ProviderClient;
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
    private final AuditingService auditingService;

    @Autowired
    public PaymentMethodTokenizationHandler(ProviderClient providerClient,
                                            CustomerPaymentMethodRepo customerPaymentMethodRepo,
                                            AuditingService auditingService){
        this.providerClient = providerClient;
        this.customerPaymentMethodRepo = customerPaymentMethodRepo;
        this.auditingService = auditingService;
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
            auditingService.log(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC, AuditingStatus.WARNING, e.toString());
            throw e;
        }

        // idempotency guard: process only if it's PROCESSING.
        if(paymentMethod.getTokenStatus() != TokenStatus.PROCESSING){
            log.info("Skip tokenization, status={}", paymentMethod.getTokenStatus());
            auditingService.log(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Tokenization skipped");
            return; // Early return
        }

        // Try to become the processor
        try {
            paymentMethod.setTokenStatus(TokenStatus.PROCESSING);
            customerPaymentMethodRepo.flush(); // will throw if another worker updated the row (version changed)
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException race) {
            log.info("Race lost; another worker started tokenization id={}", paymentMethodCreatedEvent.paymentId());
            auditingService.log(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.SUCCESSFUL, "SKIPPED_RACE_LOST id=" + paymentMethodCreatedEvent.paymentId());
            return;
        }

        // Winner does the external call
        try {
            String token = providerClient.fetchPaymentToken(paymentMethod.getProvider(), paymentMethod);
            paymentMethod.setProviderPaymentMethodToken(token);          // never log the token
            paymentMethod.setTokenStatus(TokenStatus.ACTIVE);
            customerPaymentMethodRepo.saveAndFlush(paymentMethod);
            auditingService.log(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.SUCCESSFUL, "TOKENIZED id=" + paymentMethodCreatedEvent.paymentId());
        } catch (Exception ex) {
            paymentMethod.setTokenStatus(TokenStatus.FAILED);
            customerPaymentMethodRepo.saveAndFlush(paymentMethod);
            log.warn("Tokenization failed for id={}: {}", paymentMethodCreatedEvent.paymentId(), ex.getMessage());
            auditingService.log(paymentMethodCreatedEvent.customerId(), EndpointsNameMethods.TOKENIZATION_ASYNC,
                    AuditingStatus.FAILED, "TOKENIZATION_FAILED id=" + paymentMethodCreatedEvent.paymentId());
        }
    }
}
