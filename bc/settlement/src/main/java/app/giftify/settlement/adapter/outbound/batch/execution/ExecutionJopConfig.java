package app.giftify.settlement.adapter.outbound.batch.execution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExecutionJopConfig {

    private final JobRepository jobRepository;
    private final ExecutionJobListener executionJobListener;
    private final ExecutionStepConfig stepConfig;

    @Bean
    public Job executionJob() {
        return new JobBuilder("executionJob", jobRepository)
                .listener(executionJobListener)
                .start(stepConfig.executionStep())
                .build();
    }
}
