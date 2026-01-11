package commerce.eshop.core.application.async.internal.checkout;

import commerce.eshop.core.repository.CheckoutJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CheckoutClaimService {

    // == Fields ==
    private final CheckoutJobRepo repo;

    // == Constructors ==
    @Autowired
    public CheckoutClaimService(CheckoutJobRepo repo) {
        this.repo = repo;
    }

    // == Public Methods ==
    @Transactional
    public List<Long> claim(int batchSize){
        List<Long> locked = repo.lockQueuedBatch(batchSize);
        if(locked.isEmpty())
            return List.of();
        repo.markProcessingQueuedBatch(locked);
        return locked;
    }
}
