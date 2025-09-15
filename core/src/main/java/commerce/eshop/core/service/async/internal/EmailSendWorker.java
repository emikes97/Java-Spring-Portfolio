package commerce.eshop.core.service.async.internal;

import commerce.eshop.core.model.entity.EmailsSent;
import commerce.eshop.core.repository.EmailsSentRepo;
import commerce.eshop.core.service.async.external.EmailSender;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmailSendWorker {

    // == Fields ==
    private final EmailsSentRepo emailsSentRepo;
    private final EmailSender emailSender;

    // == Constructors ==
    public EmailSendWorker(EmailsSentRepo emailsSentRepo, EmailSender emailSender){
        this.emailsSentRepo = emailsSentRepo;
        this.emailSender = emailSender;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAndMark(EmailsSent email){
        boolean ok = emailSender.sendEmail(email.getToEmail(), email.getSubject(), email.getEmailText());
        if (ok) {
            emailsSentRepo.markSent(email.getEmailId());
        } else {
            emailsSentRepo.markFailed(email.getEmailId());
        }
    }
}
