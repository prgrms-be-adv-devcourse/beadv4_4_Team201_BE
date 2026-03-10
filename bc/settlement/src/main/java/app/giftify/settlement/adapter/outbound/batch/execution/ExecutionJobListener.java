package app.giftify.settlement.adapter.outbound.batch.execution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExecutionJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Execution batch started: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Execution batch completed successfully.");
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Execution batch failed: {}", jobExecution.getAllFailureExceptions());
        }
    }
}
