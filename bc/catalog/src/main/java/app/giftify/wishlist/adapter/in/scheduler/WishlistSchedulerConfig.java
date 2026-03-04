package app.giftify.wishlist.adapter.in.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class WishlistSchedulerConfig {

    @Bean("wishlistTaskExecutor")
    public Executor wishlistTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2); // 큐가 가득차면 최대 2개까지 늘어남
        executor.setQueueCapacity(5); // 코어 스레드가 바쁠 때 대기할 수 있는 작업 수
        executor.setThreadNamePrefix("wishlist-scheduler-"); // 로그 식별
        executor.initialize();
        return executor;
    }
}
