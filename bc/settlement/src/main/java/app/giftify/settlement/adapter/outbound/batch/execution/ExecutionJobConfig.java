package app.giftify.settlement.adapter.outbound.batch.execution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class ExecutionJobConfig {

    private final JobRepository jobRepository;
    private final ExecutionJobListener executionJobListener;
    private final ExecutionStepConfig stepConfig;

    @Bean
    public Job executionJob() {
        return new JobBuilder("executionJob", jobRepository)
                .listener(executionJobListener)
                .start(stepConfig.executionPartitionStep())
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
