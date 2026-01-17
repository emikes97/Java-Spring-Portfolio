package commerce.eshop.core.application.async.internal.payment.confirmPayment;

import commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.TransactionClaimedJob;
import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.model.entity.Order;
import commerce.eshop.core.model.entity.Transaction;
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
    public void handle(UUID customerId, UUID orderId, UUID idemKey, DTOTransactionRequest dto){
        Order order = transactionClaimedJob.findOrder(idemKey, customerId, dto, orderId);
        Transaction transaction = transactionClaimedJob.createTransaction(dto, order, customerId.toString(), idemKey.toString());
        String paymentMethod = transactionClaimedJob.getPaymentMethod(dto);
        String message = "Payment by " + paymentMethod;
        PaymentExecutionRequestEvent event = new PaymentExecutionRequestEvent(transaction.getTransactionId(), transaction.getOrder().getOrderId(), customerId, paymentMethod,
                transaction.getPaymentMethod(), transaction.getTotalOutstanding(), transaction.getIdempotencyKey());
        publisher.publishEvent(event);
        transactionClaimedJob.auditSuccess(customerId, message);
    }
}
