package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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

    private final JobLauncher jobLauncher;
    private final Job validationJob;
    private final BatchProperties properties;

    /**
     * 매일 01:00시에 실행
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runValidationJob() throws Exception {
        LocalDateTime cutOffDateTime = createCutOffDateTime();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("cutOffDateTime", cutOffDateTime.toString())
                .addLong("retryLimit", (long) properties.retryLimit())
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(validationJob, jobParameters);
    }

    private @NonNull LocalDateTime createCutOffDateTime() {
        LocalTime cutOffTime = LocalTime.parse(properties.validationCutOffTime());
        return LocalDate.now().atTime(cutOffTime);
    }
}