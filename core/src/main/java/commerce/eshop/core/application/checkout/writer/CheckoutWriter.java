package commerce.eshop.core.application.checkout.writer;

import commerce.eshop.core.application.infrastructure.audit.CentralAudit;
import commerce.eshop.core.application.util.enums.AuditingStatus;
import commerce.eshop.core.model.outbox.CheckoutJob;
import commerce.eshop.core.repository.CheckoutJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CheckoutWriter {

    // == Fields ==
    private final CheckoutJobRepo repo;
    private final CentralAudit audit;

    // == Constructors ==
    @Autowired
    public CheckoutWriter(CheckoutJobRepo repo, CentralAudit audit){
        this.repo = repo;
        this.audit = audit;
    }

    // == Public Methods ==

    public CheckoutJob save(CheckoutJob job) {
        try {
            return repo.saveAndFlush(job);
        } catch (DataIntegrityViolationException ex) {
            CheckoutJob existing =
                    repo.findExistingCheckoutJob(job.getCustomerId(), job.getIdemkey()).orElse(existing = null);

            if (existing != null) {
                return existing; // idempotent replay / race
            }
            throw audit.audit(ex, job.getCustomerId(), "PROCESS_CHECKOUT", AuditingStatus.ERROR, ex.getMessage());
        }
    }
}
