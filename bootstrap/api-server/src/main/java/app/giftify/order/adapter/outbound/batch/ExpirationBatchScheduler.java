package app.giftify.order.adapter.outbound.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
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
public class ExpirationBatchScheduler {
	private static final Logger log = LoggerFactory.getLogger(ExpirationBatchScheduler.class);


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
