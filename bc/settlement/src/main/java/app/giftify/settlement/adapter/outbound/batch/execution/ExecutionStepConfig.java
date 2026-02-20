package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ExecutionStepConfig {

    private final JobRepository jobRepository;
    private final BatchProperties batchProperties;
    private final PlatformTransactionManager transactionManager;
    private final ExecutionItemConfig itemConfig;
    private final SellerIdPartitioner sellerIdPartitioner;
    private final TaskExecutor partitionExecutor;

    @Bean
    public Step executionStep() {
        return new StepBuilder("executionStep", jobRepository)
                .<Long, ExecutionResult>chunk(batchProperties.chunkSize(), transactionManager)
                .reader(itemConfig.settlementQueueReader(null))
                .processor(itemConfig.settlementExecutionProcessor())
                .writer(itemConfig.settlementExecutionWriter())
                .build();
    }

    @Bean
    public Step executionPartitionStep() {
        return new StepBuilder("executionPartitionStep", jobRepository)
                .partitioner("executionStep", sellerIdPartitioner)
                .step(executionStep())
                .taskExecutor(partitionExecutor)
                .gridSize(3)
                .build();
    }
}
