package commerce.eshop.core.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncExecutorConfig implements AsyncConfigurer {

    ////////
    /// Enable our own thread  called asyncExecutor
    /// To call our custom async, just add the below to a method you want:
    ///
    ///     Tag Async("asyncExecutor") -> Make the method Asynchronous -> Specify the name of our thread.
    ///
    ///     Tag TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) -> Ensure that it runs only after the @transactional
    /// of the caller method commits to the changes.
    ///
    ///     Tag Transactional(propagation = Propagation.REQUIRES_NEW) -> inside the async method if you need it to run in a completely
    /// separate transaction from the caller.
    ///
    ////////

    @Bean(name="asyncExecutor")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor ex = createThreadPoolTask("async-");
        return ex;
    }

    @Bean(name = "emailExecutor")
    public Executor emailExecutor(){
        ThreadPoolTaskExecutor ex = createThreadPoolTask("async-email-");
        return ex;
    }

    @Bean(name = "checkoutJobExecutor")
    public Executor checkoutJobExecutor(){
        ThreadPoolTaskExecutor ex = createThreadPoolTask("async-order-");
        return ex;
    }

    @Bean(name = "orderExecutor")
    public Executor orderExecutor(){
        ThreadPoolTaskExecutor ex = createThreadPoolTask("async-order-");
        return ex;
    }

    @Bean(name = "transactionExecutor")
    public Executor transactionExecutor(){
        ThreadPoolTaskExecutor ex = createThreadPoolTask("async-transaction-");
        return ex;
    }

    // == Public Methods ==

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return (ex, method, params) -> System.err.println("[ASYNC-UNCAUGHT] " + method + " -> " + ex.getMessage());
    }

    // == Private Methods ==
    // Keep the rest as non changing for now and change only names, after we finalize the async refactoring we will expand on threadpools.
    private ThreadPoolTaskExecutor createThreadPoolTask(String threadName){
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix(threadName);

        // IO-Bound work (HTTP Calls): allow higher concurrency
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(200); //  * - Queue capacity: 200 (backlog of tasks before rejecting).
        ex.setKeepAliveSeconds(60); // * - Keep alive: 60 seconds (time before idle threads are removed).
        ex.setAwaitTerminationSeconds(30); // * - Await termination: 30 seconds (time to wait for tasks to finish on shutdown).
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}
