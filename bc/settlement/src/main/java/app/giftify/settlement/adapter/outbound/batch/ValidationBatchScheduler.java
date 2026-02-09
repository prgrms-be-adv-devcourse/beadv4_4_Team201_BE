package app.giftify.settlement.adapter.outbound.batch;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class ValidationBatchScheduler {

    @Value("${settlement.batch.retry-limit}")
    private Long retryLimit;
    @Value("${settlement.batch.validation-cut-off-time}")
    private String cutOffTimeStr;

    private final JobLauncher jobLauncher;
    private final Job validationJob;

    /**
     * 매일 01:00시에 실행
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runValidationJob() throws Exception {
        LocalDateTime cutOffDateTime = createCutOffDateTime();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("cutOffDateTime", cutOffDateTime.toString())
                .addLong("retryLimit", retryLimit)
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(validationJob, jobParameters);
    }

    private @NonNull LocalDateTime createCutOffDateTime() {
        LocalTime cutOffTime = LocalTime.parse(cutOffTimeStr);
        return LocalDate.now().atTime(cutOffTime);
    }
}