package commerce.eshop.core.application.async.internal.payment.confirmPayment;

import commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.confirmPayment.TransactionClaimedJob;
import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.web.dto.requests.Transactions.DTOTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ConfirmPaymentOrchestrator {

    // == Fields ==
    private final TransactionClaimedJob transactionClaimedJob;
    private final ApplicationEventPublisher publisher;

    // == Constructors ==
    @Autowired
    public ConfirmPaymentOrchestrator(TransactionClaimedJob transactionClaimedJob, ApplicationEventPublisher publisher) {
        this.transactionClaimedJob = transactionClaimedJob;
        this.publisher = publisher;
    }

    // == Public Methods ==
    public void handle(UUID customerId, UUID orderId, UUID idemKey, long jobId){
        CheckoutJob job = transactionClaimedJob.fetchJob(jobId);
        if(job.getState() != CheckoutStates.ORDER_CREATED)
            return;
        job.setState(CheckoutStates.TRANSACTION_READY);
        job = transactionClaimedJob.saveState(job);
        DTOTransactionRequest dto = job.toTransactionRequest(job.getCustomerPayment());
        Order order = transactionClaimedJob.findOrder(idemKey, customerId, dto, orderId);
        Transaction transaction = transactionClaimedJob.createTransaction(dto, order, customerId.toString(), idemKey.toString());
        String paymentMethod = transactionClaimedJob.getPaymentMethod(dto);
        String message = "Payment by " + paymentMethod;
        PaymentExecutionRequestEvent event = new PaymentExecutionRequestEvent(transaction.getTransactionId(), transaction.getOrder().getOrderId(), customerId, paymentMethod,
                transaction.getPaymentMethod(), transaction.getTotalOutstanding(), transaction.getIdempotencyKey(), job.getId());
        publisher.publishEvent(event);
        transactionClaimedJob.auditSuccess(customerId, message);
    }
}
