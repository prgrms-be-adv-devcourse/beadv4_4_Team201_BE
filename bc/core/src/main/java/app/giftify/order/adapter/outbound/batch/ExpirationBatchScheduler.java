package app.giftify.order.adapter.outbound.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class ExpirationBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job expirationJob;

    @Value("${batch.order-expiration.threshold-minutes}")
    private Long thresholdMinutes;

    /**
     * 작업이 끝난 시점부터 30분을 카운트
     */
    @Scheduled(fixedDelayString = "${batch.order-expiration.interval-minutes}", timeUnit = TimeUnit.MINUTES)
    public void runExecutionJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .addLong("thresholdMinutes", thresholdMinutes)
                .toJobParameters();

        try {
            jobLauncher.run(expirationJob, jobParameters);
        } catch (Exception e) {
            log.error(">>> [주문 만료 배치 실패] 배치 실행에 실패해 종료되었습니다.", e);
        }
    }
}
