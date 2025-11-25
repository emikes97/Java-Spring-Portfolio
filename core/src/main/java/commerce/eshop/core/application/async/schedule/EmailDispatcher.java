package commerce.eshop.core.application.async.schedule;

import commerce.eshop.core.application.email.EmailClaimService;
import commerce.eshop.core.model.entity.EmailsSent;
import commerce.eshop.core.repository.EmailsSentRepo;
import commerce.eshop.core.application.async.internal.email.EmailSendWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class EmailDispatcher {

    // == Fields ==
    private final EmailsSentRepo emailsSentRepo;
    private final int batchSize;
    private final Executor exec;
    private final EmailClaimService emailClaimService;
    private final EmailSendWorker emailSendWorker;

    // == Constructors ==
    @Autowired
    public EmailDispatcher(EmailsSentRepo emailsSentRepo,
                           @Qualifier("emailExecutor") Executor exec,
                           @Value("${emails.dispatch.batch-size:25}") int batchSize,
                           EmailClaimService emailClaimService, EmailSendWorker emailSendWorker) {
        this.emailsSentRepo = emailsSentRepo;
        this.exec = exec;
        this.batchSize = batchSize;
        this.emailClaimService = emailClaimService;
        this.emailSendWorker = emailSendWorker;
    }

    // == Public Scheduled Methods ==
    @Scheduled(fixedDelayString = "${emails.dispatch.delay-ms:2000}")
    public void dispatchEmailTick() {
        // 1) If there are queued emails, claim and process them
        if (emailsSentRepo.queuesExists()) {
            var batch = emailClaimService.claim(batchSize); // TX inside service
            submitBatch(batch);
            return;
        }

        // 2) Otherwise, try to rescue stuck 'SENDING' rows (age-gated)
        if (emailsSentRepo.hasStuckSending()) {
            var rescued = emailClaimService.rescue(batchSize);
            submitBatch(rescued);
        }
    }

    private void submitBatch(List<EmailsSent> batch) {
        if (batch == null || batch.isEmpty()) return;

        log.info("Dispatching {} emails", batch.size());
        for (var e : batch) {
            CompletableFuture
                    .runAsync(() -> emailSendWorker.sendAndMark(e), exec) // enters TX via proxy
                    .exceptionally(ex -> {
                        log.error("Email send task crashed for id {}", e.getEmailId(), ex);
                        try {
                            emailsSentRepo.markFailed(e.getEmailId());
                        } catch (Exception ignore) {
                        }
                        return null;
                    });
        }
    }
}
