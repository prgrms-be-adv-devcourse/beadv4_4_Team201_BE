package app.giftify.order.adapter.outbound.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExpirationBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job expirationJob;

    /**
     * 작업이 끝난 시점부터 30분을 카운트
     */
    @Scheduled(fixedDelay = 1800000)
    public void runExecutionJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(expirationJob, jobParameters);
    }
}
