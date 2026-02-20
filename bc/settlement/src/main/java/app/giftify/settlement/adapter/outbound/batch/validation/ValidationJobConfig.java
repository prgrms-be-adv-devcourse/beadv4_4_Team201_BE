package app.giftify.settlement.adapter.outbound.batch.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
