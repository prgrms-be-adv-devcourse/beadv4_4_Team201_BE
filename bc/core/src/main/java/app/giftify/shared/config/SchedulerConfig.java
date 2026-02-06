package app.giftify.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        // 스레드 풀 사이즈 설정 (필요에 따라 조정 가능)
        threadPoolTaskScheduler.setPoolSize(5);

        // 스레드 이름 접두서 설정
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-");

        // 초기화
        threadPoolTaskScheduler.initialize();

        // 스프링 스케줄러로 등록
        taskRegistrar.setScheduler(threadPoolTaskScheduler);
    }
}
