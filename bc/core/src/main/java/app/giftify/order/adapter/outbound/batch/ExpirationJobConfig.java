package app.giftify.order.adapter.outbound.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ExpirationJobConfig {

    private final JobRepository jobRepository;
    private final Step expirationStep;
    private final ExpirationJobListener jobListener;

    @Bean
    public Job expirationJob() {
        return new JobBuilder("expirationJob", jobRepository)
                .listener(jobListener)
                .start(expirationStep)
                .build();
    }
}
