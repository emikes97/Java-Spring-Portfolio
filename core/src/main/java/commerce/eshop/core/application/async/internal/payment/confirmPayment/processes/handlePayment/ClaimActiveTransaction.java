package commerce.eshop.core.application.async.internal.payment.confirmPayment.processes.handlePayment;

import commerce.eshop.core.application.checkout.writer.CheckoutWriter;
import commerce.eshop.core.application.infrastructure.DomainLookupService;
import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.constants.EndpointsNameMethods;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.application.util.enums.TransactionStatus;
import commerce.eshop.core.model.entity.Transaction;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.model.states.CheckoutStates;
import commerce.eshop.core.repository.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClaimActiveTransaction {

    // == Fields ==
    private final TransactionRepo transactionRepo;
    private final DomainLookupService domainLookupService;
    private final CheckoutWriter writer;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public ClaimActiveTransaction(TransactionRepo transactionRepo, DomainLookupService domainLookupService, CheckoutWriter writer, CentralAudit centralAudit) {
        this.transactionRepo = transactionRepo;
        this.domainLookupService = domainLookupService;
        this.writer = writer;
        this.centralAudit = centralAudit;
    }

    // Public Methods ==

    public Transaction handle(String idemKey, UUID customerId, UUID orderId, long jobId){

        final Transaction tr;

        try{

            CheckoutJob job = domainLookupService.getCheckoutJob(jobId, "ProcessPayment");

            if (job.getState() != CheckoutStates.TRANSACTION_READY)
                throw new IllegalStateException("Transaction is already processing");

            job.setState(CheckoutStates.TRANSACTION_PROCESSING);

            tr = transactionRepo.findByIdempotencyKeyForUpdate(idemKey).orElseThrow(() -> new IllegalArgumentException("No transaction with idemKey = " + idemKey));

            if(!orderId.equals(tr.getOrder().getOrderId())){
                throw  new IllegalStateException("Transaction IDs mismatch");
            }

            if(tr.getStatus() == TransactionStatus.SUCCESSFUL || tr.getStatus() == TransactionStatus.FAILED){
                throw new IllegalStateException("Transaction is already completed, request will be ignored");
            }
            writer.save(job);
            return tr;

        }catch (IllegalStateException | IllegalArgumentException exception){
            throw centralAudit.audit(exception, customerId, EndpointsNameMethods.PAYMENT_PROCESSING_ASYNC, AuditingStatus.ERROR, exception.toString());
        }
    }
}
