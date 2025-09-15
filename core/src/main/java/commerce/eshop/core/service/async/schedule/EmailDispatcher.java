package commerce.eshop.core.service.async.schedule;

import commerce.eshop.core.model.entity.EmailsSent;
import commerce.eshop.core.repository.EmailsSentRepo;
import commerce.eshop.core.service.async.external.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class EmailDispatcher {

    // == Fields ==
    private final EmailsSentRepo emailsSentRepo;
    private final EmailSender emailSender;
    private final int batchSize;
    private final Executor exec;

    // == Constructors ==
    @Autowired
    public EmailDispatcher(EmailsSentRepo emailsSentRepo, EmailSender emailSender,
                           @Qualifier("emailExecutor") Executor exec,
                           @Value("${emails.dispatch.batch-size:25}") int batchSize){
        this.emailsSentRepo = emailsSentRepo;
        this.emailSender = emailSender;
        this.exec = exec;
        this.batchSize = batchSize;
    }

    // == Public Scheduled Methods ==
    @Scheduled(fixedDelayString = "${emails.dispatch.delay-ms:2000}")
    public void dispatchEmailTick(){
        var batch = emailsSentRepo.claimBatch(batchSize);               // tune via config
        for (var e : batch) {
            CompletableFuture.runAsync(() -> sendOne(e), exec);
        }
    }

    // == Private Methods

    private void sendOne(EmailsSent email){
        boolean ok = emailSender.sendEmail(email.getToEmail(), email.getSubject(), email.getEmailText());
        if (ok) {
            emailsSentRepo.markSent(email.getEmailId());
        } else {
            emailsSentRepo.markFailed(email.getEmailId());
        }
    }
}
