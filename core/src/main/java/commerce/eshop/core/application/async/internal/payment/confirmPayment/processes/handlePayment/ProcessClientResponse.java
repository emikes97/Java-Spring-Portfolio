package commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment;

import commerce.eshop.core.application.checkout.writer.CheckoutWriter;
import commerce.eshop.core.application.email.EmailComposer;
import commerce.eshop.core.application.events.email.EmailEventRequest;
import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.application.events.payments.PaymentSucceededOrFailed;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.OrderStatus;
import commerce.eshop.core.application.util.enums.TransactionStatus;
import commerce.eshop.core.model.entity.Customer;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.repository.TransactionRepo;
import commerce.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@Slf4j
public class ProcessClientResponse {

    // == Fields ==
    private final TransactionRepo repo;
    private final ApplicationEventPublisher publisher;
    private final CheckoutWriter writer;
    private final EmailComposer emailComposer;
    private final DomainLookupService domainLookupService;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public ProcessClientResponse(TransactionRepo repo, ApplicationEventPublisher publisher, CheckoutWriter writer, EmailComposer emailComposer, DomainLookupService domainLookupService, CentralAudit centralAudit) {
        this.repo = repo;
        this.publisher = publisher;
        this.writer = writer;
        this.emailComposer = emailComposer;
        this.domainLookupService = domainLookupService;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==

    public void process(ProviderChargeResult providerChargeResult, Transaction tr, PaymentExecutionRequestEvent event){

        CheckoutJob job = domainLookupService.getCheckoutJob(event.jobId(), "Process Client Response");

        if(job.getState() != CheckoutStates.TRANSACTION_PROCESSING){
            throw new IllegalStateException("Transaction is already processing");
        }

        if(providerChargeResult.successful()){
            tr.setStatus(TransactionStatus.SUCCESSFUL);
            tr.setCompletedAt(OffsetDateTime.now());
            tr.setProviderReference(providerChargeResult.providerReference());
            log.info("Transaction: " + tr.getTransactionId() + "was completed");
            repo.save(tr);
            job.setState(CheckoutStates.COMPLETED);
            writer.save(job);
            publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAID, OffsetDateTime.now(), tr.getOrder().getOrderId()));
            publishPaymentEmail(event, tr, true);
            centralAudit.info(event.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.SUCCESSFUL,
                    "Transaction Succeeded");
        } else {
            tr.setStatus(TransactionStatus.FAILED);
            tr.setCompletedAt(OffsetDateTime.now());
            tr.setProviderReference(providerChargeResult.providerReference());
            log.info("Transaction: " + tr.getTransactionId() + "failed");
            repo.save(tr);
            centralAudit.info(event.customerId(), EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.FAILED,
                    "Transaction Failed");
            job.setState(CheckoutStates.FAILED);
            writer.save(job);
            publisher.publishEvent(new PaymentSucceededOrFailed(OrderStatus.PAYMENT_FAILED, OffsetDateTime.now(), tr.getOrder().getOrderId()));
            publishPaymentEmail(event, tr, false);
        }
    }

    // == Private Methods ==

    private void publishPaymentEmail(PaymentExecutionRequestEvent pm, Transaction tr, Boolean successful){
        Customer customer = domainLookupService.getCustomerOrThrow(pm.customerId(), EndpointsNameMethods.GET_PROFILE_BY_ID);
        Order order = domainLookupService.getOrderOrThrow(customer.getCustomerId(), pm.orderId(), EndpointsNameMethods.ORDER_VIEW);
        EmailEventRequest event = null;
        if(successful){
            event = emailComposer.paymentConfirmed(customer, order, tr, tr.getTotalOutstanding(), "Euro");
        } else {
            event = emailComposer.paymentFailed(customer, order, tr, "Transaction Failed");
        }
        publisher.publishEvent(event);
    }
}
