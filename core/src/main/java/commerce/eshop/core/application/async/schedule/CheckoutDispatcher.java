package commerce.eshop.core.application.async.schedule;

import commerce.eshop.core.application.async.internal.checkout.CheckoutClaimService;
import commerce.eshop.core.application.async.internal.order.createNewOrder.OrderCreationOrchestrator;
import commerce.eshop.core.repository.CheckoutJobRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class CheckoutDispatcher {

    // == Fields ==
    private final CheckoutJobRepo repo;
    private final int batchSize;
    private final Executor exec;
    private final CheckoutClaimService claimService;
    private final OrderCreationOrchestrator order;

    // == Constructors ==
    @Autowired
    public CheckoutDispatcher(CheckoutJobRepo repo, @Value("${checkoutjobs.dispatch.batch-size}") int batchSize, @Qualifier("checkoutJobExecutor") Executor exec,
                              CheckoutClaimService claimService, OrderCreationOrchestrator order) {
        this.repo = repo;
        this.batchSize = batchSize;
        this.exec = exec;
        this.claimService = claimService;
        this.order = order;
    }

    // == Public Methods ==
    @Scheduled(fixedDelayString = "${checkoutjobs.dispatch.delay-ms}")
    public void dispatchCheckoutJobs(){
        if(repo.checkIfCheckoutJobsArePending()){
            List<Long> pendingJobs = claimService.claim(batchSize);
            if (pendingJobs == null || pendingJobs.isEmpty()) return;
            submitBatch(pendingJobs);
            return;
        }

        // check for stucked jobs
    }

    // == Private Methods ==

    private void submitBatch(List<Long> jobs){
        log.info("Dispatching {} emails", jobs.size());

        for (long e : jobs){
            CompletableFuture
                    .runAsync(() -> order.createOrder(e), exec)
                    .exceptionally(ex -> {
                        log.error("Checkout send task crashed for id {}", e, ex);
                        return null;
                    });
        }
    }
}
