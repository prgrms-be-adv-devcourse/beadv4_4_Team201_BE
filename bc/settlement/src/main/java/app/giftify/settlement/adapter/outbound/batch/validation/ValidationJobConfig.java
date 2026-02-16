package app.giftify.settlement.adapter.outbound.batch.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RequiredArgsConstructor
@Configuration
public class ValidationJobConfig {

    private final JobRepository jobRepository;
    private final ValidationStepConfig stepConfig;
    private final ValidationJobListener validationJobListener;

    @Bean
    public Job validationJob() {
        return new JobBuilder("validationJob", jobRepository)
                .listener(validationJobListener)
                .start(stepConfig.cleanupSettlementQueueStep())
                .next(stepConfig.validationPartitionStep())
                .build();
    }

    @Bean
    public TaskExecutor partitionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("partition-v-");
        executor.initialize();
        return executor;
    }
}
