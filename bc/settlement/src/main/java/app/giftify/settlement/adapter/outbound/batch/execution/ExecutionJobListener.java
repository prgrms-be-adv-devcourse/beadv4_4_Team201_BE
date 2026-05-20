package app.giftify.settlement.adapter.outbound.batch.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class ExecutionJobListener implements JobExecutionListener {
	private static final Logger log = LoggerFactory.getLogger(ExecutionJobListener.class);


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
