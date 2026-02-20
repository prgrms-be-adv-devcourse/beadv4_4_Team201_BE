package app.giftify.settlement.adapter.outbound.batch.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ValidationJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Validation batch started: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Validation batch completed successfully.");
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Validation batch failed: {}", jobExecution.getAllFailureExceptions());
        }
    }
}
