package commerse.eshop.core.service.async.internal;

import commerse.eshop.core.events.PaymentExecutionRequestEvent;
import commerse.eshop.core.model.entity.Transaction;
import commerse.eshop.core.repository.TransactionRepo;
import commerse.eshop.core.service.async.external.PaymentProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
        if(paymentExecutionRequestEvent.orderId().equals(tr.getOrder().getOrderId())){
            try{
                // Initiate third party payment process.
            } catch (DataIntegrityViolationException err){
                throw new IllegalStateException(err);
            }
        }
    }

}
