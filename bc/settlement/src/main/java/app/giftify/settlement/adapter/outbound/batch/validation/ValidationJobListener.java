package app.giftify.settlement.adapter.outbound.batch.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class ValidationJobListener implements JobExecutionListener {
	private static final Logger log = LoggerFactory.getLogger(ValidationJobListener.class);

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
