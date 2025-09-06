package commerse.eshop.core.service.async.internal;

import commerse.eshop.core.events.auditing_events.AuditingImmediateEvent;
import commerse.eshop.core.events.auditing_events.AuditingMethodEvent;
import commerse.eshop.core.model.entity.Auditing;
import commerse.eshop.core.repository.AuditingRepo;
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
    public AuditEventListener(AuditingRepo auditingRepo, Auditing auditing){
        this.auditingRepo = auditingRepo;
    }

    /// Successful / Failed events
    @Async
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
    @Async
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
