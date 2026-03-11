package app.giftify.settlement.adapter.outbound.batch.execution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class ExecutionBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job executionJob;

    /**
     * 매일 02:00시에 실행
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runExecutionJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(executionJob, jobParameters);
    }
}
