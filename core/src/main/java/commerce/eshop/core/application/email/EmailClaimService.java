package commerce.eshop.core.application.email;

import commerce.eshop.core.model.entity.EmailsSent;
import commerce.eshop.core.repository.EmailsSentRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class EmailClaimService {

    private final EmailsSentRepo emailsSentRepo;

    public EmailClaimService(EmailsSentRepo emailsSentRepo){
        this.emailsSentRepo = emailsSentRepo;
    }

    @Transactional
    public List<EmailsSent> claim(int batch) {
        var locked = emailsSentRepo.lockQueuedBatch(batch);
        if (locked.isEmpty()) return List.of();
        var ids = locked.stream().map(EmailsSent::getEmailId).toArray(UUID[]::new);
        emailsSentRepo.markSendingBatch(ids);
        return locked;
    }

    @Transactional(readOnly = true)
    public List<EmailsSent> rescue(int batch){
        return emailsSentRepo.rescueLockedBatch(batch);
    }
}
