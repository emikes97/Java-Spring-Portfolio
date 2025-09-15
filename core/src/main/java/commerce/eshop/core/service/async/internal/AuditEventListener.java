package commerce.eshop.core.service.async.internal;

import commerce.eshop.core.events.auditing_events.AuditingImmediateEvent;
import commerce.eshop.core.events.auditing_events.AuditingMethodEvent;
import commerce.eshop.core.model.entity.Auditing;
import commerce.eshop.core.repository.AuditingRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class AuditEventListener {

    private final AuditingRepo auditingRepo;

    @Autowired
    public AuditEventListener(AuditingRepo auditingRepo){
        this.auditingRepo = auditingRepo;
    }

    /// Successful / Failed events
    @Async("asyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditingMethodEvent event){
        try {
            Auditing auditing = new Auditing(event.customerId(), event.methodName(), event.status(), event.message());
            auditingRepo.save(auditing);
        } catch (DataAccessException exception){
            log.error("Failed to save audit entry: {}", event, exception);
        }
    }

    /// Edge cases / errors with reason ///
    @Async("asyncExecutor")
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(AuditingImmediateEvent event){
        try {
            Auditing auditing = new Auditing(event.customerId(), event.methodName(), event.status(), event.reason());
            auditingRepo.save(auditing);
        } catch (DataAccessException exception){
            log.error("Failed to save audit entry: {}", event, exception);
        }
    }
}
