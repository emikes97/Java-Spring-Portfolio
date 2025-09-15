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
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("async-"); // Adds async- as a prefix to thread name, to make it easier for our info/debug.

        // IO-Bound work (HTTP calls): allow higher concurrency
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(200); //  * - Queue capacity: 200 (backlog of tasks before rejecting).
        ex.setKeepAliveSeconds(60); // * - Keep alive: 60 seconds (time before idle threads are removed).
        ex.setAwaitTerminationSeconds(30); // * - Await termination: 30 seconds (time to wait for tasks to finish on shutdown).
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.initialize();
        return ex;
    }

    @Bean(name = "emailExecutor")
    public Executor emailExecutor(){
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("async-email-"); // Adds async- as a prefix to thread name, to make it easier for our info/debug.

        // IO-Bound work (HTTP calls): allow higher concurrency
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(200); //  * - Queue capacity: 200 (backlog of tasks before rejecting).
        ex.setKeepAliveSeconds(60); // * - Keep alive: 60 seconds (time before idle threads are removed).
        ex.setAwaitTerminationSeconds(30); // * - Await termination: 30 seconds (time to wait for tasks to finish on shutdown).
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.initialize();
        return ex;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return (ex, method, params) -> System.err.println("[ASYNC-UNCAUGHT] " + method + " -> " + ex.getMessage());
    }
}
