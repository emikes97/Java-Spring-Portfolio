package commerce.eshop.core.application.async.schedule;

import commerce.eshop.core.application.async.internal.checkout.CheckoutClaimService;
import commerce.eshop.core.repository.CheckoutJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class CheckoutDispatcher {

    // == Fields ==
    private final CheckoutJobRepo repo;
    private final int batchSize;
    private final Executor exec;
    private final CheckoutClaimService claimService;
    private final CheckoutProcessService processService;

    // == Constructors ==
    @Autowired
    public CheckoutDispatcher(CheckoutJobRepo repo, @Value("${checkoutjobs.dispatch.batch-size}") int batchSize, @Qualifier("checkoutJobExecutor") Executor exec,
                              CheckoutClaimService claimService, CheckoutProcessService processService) {
        this.repo = repo;
        this.batchSize = batchSize;
        this.exec = exec;
        this.claimService = claimService;
        this.processService = processService;
    }
    // == Public Methods ==
    @Scheduled(fixedDelayString = "${checkoutjobs.dispatch.delay-ms}")
    public void dispatchCheckoutJobs(){
        if(repo.checkIfCheckoutJobsArePending()){
            List<Long> pendingJobs = claimService.claim(batchSize);
            if(pendingJobs.isEmpty())
                return;
            // send to worker.
        }

        // check for stucked jobs
    }

    // == Private Methods ==
}
