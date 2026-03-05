package app.giftify.order.adapter.outbound.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class ExpirationJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>> [배치 시작] 주문 만료 작업 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long duration = Duration.between(
                jobExecution.getStartTime(),
                jobExecution.getEndTime()
        ).toMillis();

        // 모든 Step의 실행 통계 합산
        int totalReadCount = 0;
        int totalWriteCount = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            totalReadCount += (int) stepExecution.getReadCount();
            totalWriteCount += (int) stepExecution.getWriteCount();
        }

        log.info("=================================================");
        log.info(">>> [배치 종료] 주문 만료 작업 완료");
        log.info(">>> 실행 상태: {}", jobExecution.getStatus());
        log.info(">>> 소요 시간: {}ms", duration);
        log.info(">>> 읽은 주문 건수: {}", totalReadCount);
        log.info(">>> 만료 처리 건수: {}", totalWriteCount);

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("!!! [배치 실패] 원인: {}", jobExecution.getAllFailureExceptions());
        }
        log.info("=================================================");
    }
}
