package app.giftify.order.adapter.outbound.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExpirationJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("주문 만료 배치 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("주문 만료 배치 종료: {}", jobExecution.getJobInstance().getJobName());
    }
}
