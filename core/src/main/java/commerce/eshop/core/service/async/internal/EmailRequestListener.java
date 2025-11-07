package commerce.eshop.core.service.async.internal;

import commerce.eshop.core.events.EmailEventRequest;
import commerce.eshop.core.model.entity.EmailsSent;
import commerce.eshop.core.repository.EmailsSentRepo;
import commerce.eshop.core.util.CentralAudit;
import commerce.eshop.core.util.enums.AuditingStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.validation.Validator;

import java.util.Set;

@Component
public class EmailRequestListener {

    // == Fields ==
    private final Validator validator;
    private final EmailsSentRepo emailsSentRepo;
    private final CentralAudit centralAudit;

    // == Constructors ==
    @Autowired
    public EmailRequestListener(Validator validator, EmailsSentRepo emailsSentRepo, CentralAudit centralAudit){
        this.validator = validator;
        this.emailsSentRepo = emailsSentRepo;
        this.centralAudit = centralAudit;
    }

    // == Public Methods ==
    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(EmailEventRequest request){

        Set<ConstraintViolation<EmailEventRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            ConstraintViolationException violation = new ConstraintViolationException(violations);
            throw centralAudit.audit(violation, request.customerId(), "Async-Email", AuditingStatus.ERROR, violation.toString());
        }

        try {
            EmailsSent emailsSent = new EmailsSent(request.customerId(), request.orderOrPaymentId(), request.customerName(), request.toEmail(),
                    request.type(), request.subject(), request.emailText());
            emailsSentRepo.save(emailsSent);
            centralAudit.info(request.customerId(), "Async-Email", AuditingStatus.SUCCESSFUL, "EMAIL_ENQUEUED");
        } catch (DataIntegrityViolationException | IllegalArgumentException bad){
            throw centralAudit.audit(bad, request.customerId(), "Async-Email", AuditingStatus.ERROR, bad.getMessage());
        }
    }
}
