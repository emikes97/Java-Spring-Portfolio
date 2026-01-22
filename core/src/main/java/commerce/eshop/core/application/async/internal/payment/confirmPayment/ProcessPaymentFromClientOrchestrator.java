package commerce.eshop.core.application.async.internal.payment.confirmPayment;

import commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment.CheckPaymentStatusFromClient;
import commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment.ClaimActiveTransaction;
import commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment.ProcessClientResponse;
import commerce.eshop.core.application.events.payments.PaymentExecutionRequestEvent;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.web.dto.response.Providers.Charging.ProviderChargeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Optional;


@Component
@Slf4j
public class ProcessPaymentFromClientOrchestrator {

    // == Fields ==
    private final ClaimActiveTransaction claimActiveTransaction;
    private final CheckPaymentStatusFromClient client;
    private final ProcessClientResponse processClientResponse;

    // == Constructors ==
    @Autowired
    public ProcessPaymentFromClientOrchestrator(ClaimActiveTransaction claimActiveTransaction, CheckPaymentStatusFromClient client, ProcessClientResponse processClientResponse) {
        this.claimActiveTransaction = claimActiveTransaction;
        this.client = client;
        this.processClientResponse = processClientResponse;
    }

    // == Public Methods ==
    @Async("transactionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPayment(PaymentExecutionRequestEvent paymentExecutionRequestEvent) {

        final Transaction tr = claimActiveTransaction.handle(paymentExecutionRequestEvent.idemKey(), paymentExecutionRequestEvent.customerId(), paymentExecutionRequestEvent.orderId(), paymentExecutionRequestEvent.jobId());
        Map<String, Object> snap = Optional.ofNullable(paymentExecutionRequestEvent.snapshot()).orElse(Map.of());
        ProviderChargeResult result = client.handle(snap, paymentExecutionRequestEvent.methodType(), tr, paymentExecutionRequestEvent.customerId());
        processClientResponse.process(result, tr, paymentExecutionRequestEvent);
    }
}
