package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.shared.api.exception.InfraException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ValidationStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final OrderIdPartitioner orderIdPartitioner;
    private final ValidationBulkLoadListener validationBulkLoadListener;
    private final ValidationItemConfig itemConfig;
    private final BatchProperties properties;
    private final TaskExecutor partitionExecutor;

    @Bean
    public Step validationStep() {
        return new StepBuilder("validationStep", jobRepository)
                .<SettlementItem, SettlementQueue>chunk(properties.chunkSize(), transactionManager)
                .reader(itemConfig.settlementItemReader(null, null, null))
                .processor(itemConfig.validationProcessor(null))
                .writer(itemConfig.validationWriter())
                .listener(validationBulkLoadListener)
                .faultTolerant()
                .retry(InfraException.class)
                .retryLimit(properties.retryLimit())
                .build();
    }

    @Bean
    public Step validationPartitionStep() {
        return new StepBuilder("validationPartitionStep", jobRepository)
                .partitioner("validationStep", orderIdPartitioner)
                .step(validationStep())
                .taskExecutor(partitionExecutor)
                .gridSize(3)
                .build();
    }

    @Bean
    public Step cleanupSettlementQueueStep() {
        return new StepBuilder("cleanupSettlementQueueStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    EntityManager em = EntityManagerFactoryUtils
                            .getTransactionalEntityManager(emf);
                    em.createNativeQuery("TRUNCATE TABLE settlement_queue").executeUpdate();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
