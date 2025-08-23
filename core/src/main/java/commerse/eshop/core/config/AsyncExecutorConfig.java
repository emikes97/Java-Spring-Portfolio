package commerse.eshop.core.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncExecutorConfig implements AsyncConfigurer {

    @Bean(name="asyncExecutor")
    public Executor asyncExecutor(){
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("async-");
        // IO-Bound work (HTTP calls): allow higher concurrency
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(200);
        ex.setKeepAliveSeconds(60);
        ex.setAwaitTerminationSeconds(30);
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.initialize();
        return ex;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler(){
        return (ex, method, params) -> System.err.println("[ASYNC-UNCAUGHT] " + method + " -> " + ex.getMessage());
    }
}
