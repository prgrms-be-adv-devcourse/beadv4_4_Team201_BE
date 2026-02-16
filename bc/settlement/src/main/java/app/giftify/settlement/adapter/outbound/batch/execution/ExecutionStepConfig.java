package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class ExecutionStepConfig {

    private final JobRepository jobRepository;
    private final BatchProperties batchProperties;
    private final PlatformTransactionManager transactionManager;
    private final ExecutionItemConfig itemConfig;

    @Bean
    public Step executionStep() {
        return new StepBuilder("executionStep", jobRepository)
                .<Long, ExecutionResult>chunk(batchProperties.chunkSize(), transactionManager)
                .reader(itemConfig.sellerIdReader(null))
                .processor(itemConfig.settlementExecutionProcessor())
                .writer(itemConfig.settlementExecutionWriter())
                .build();
    }
}
