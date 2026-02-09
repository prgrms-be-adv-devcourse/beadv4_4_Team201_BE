package app.giftify.settlement.adapter.outbound.batch;

import app.giftify.settlement.adapter.inbound.event.ValidationJobListener;
import app.giftify.settlement.domain.SettlementItem;
import app.giftify.settlement.domain.SettlementQueue;
import app.giftify.settlement.domain.exception.InfraException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ValidationBatchConfig {

    @Value("${settlement.batch.chunk-size}")
    private int chunkSize;
    @Value("${settlement.batch.retry-limit}")
    private int retryLimit;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final OrderIdPartitioner orderIdPartitioner;
    private final ValidationBulkLoadListener validationBulkLoadListener;
    private final ValidationJobListener validationJobListener;

    @Bean
    public Job validationJob() {
        return new JobBuilder("validationJob", jobRepository)
                .listener(validationJobListener)
                .start(validationPartitionStep())
                .next(cleanupSettlementQueueStep(emf))
                .build();
    }

    @Bean
    public Step validationStep() {
        return new StepBuilder("validationStep", jobRepository)
                .<SettlementItem, SettlementQueue>chunk(chunkSize, transactionManager)
                .reader(settlementItemReader(null, null, null))
                .processor(validationProcessor(null))
                .writer(validationWriter())
                .listener(validationBulkLoadListener)
                .faultTolerant()
                .retry(InfraException.class)
                .retryLimit(retryLimit)
                .build();
    }

    @Bean
    public Step validationPartitionStep() {
        return new StepBuilder("validationPartitionStep", jobRepository)
                .partitioner("validationStep", orderIdPartitioner)
                .step(validationStep())
                .taskExecutor(partitionExecutor())
                .gridSize(3)
                .build();
    }

    @Bean
    public TaskExecutor partitionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("partition-v-");
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SettlementItem> settlementItemReader(
            @Value("#{stepExecutionContext['minOrderId']}") Long minOrderId,
            @Value("#{stepExecutionContext['maxOrderId']}") Long maxOrderId,
            @Value("#{jobParameters['cutOffDateTime']}") LocalDateTime cutOffDateTime
    ) {
        return new JpaPagingItemReaderBuilder<SettlementItem>()
                .name("settlementItemReader")
                .entityManagerFactory(emf)
                .queryString("""
                    SELECT s FROM SettlementItem s
                    WHERE s.orderId BETWEEN :minOrderId AND :maxOrderId
                      AND (s.lifeCycleMeta.status = 'PENDING'
                           OR (s.lifeCycleMeta.status = 'FAIL' AND s.retryCount < :retryLimit))
                      AND s.createdAt < :cutOffDateTime
                """)
                .parameterValues(Map.of(
                        "minOrderId", minOrderId,
                        "maxOrderId", maxOrderId,
                        "retryLimit", retryLimit,
                        "cutOffDateTime", cutOffDateTime
                ))
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<SettlementItem, SettlementQueue> validationProcessor(
            ValidationAmountContext cache
    ) {
        return new OrderValidationProcessor(cache);
    }

    @Bean
    public JpaItemWriter<SettlementQueue> validationWriter() {
        return new JpaItemWriterBuilder<SettlementQueue>()
                .entityManagerFactory(emf)
                .build();
    }

    @Bean
    public Step cleanupSettlementQueueStep(EntityManagerFactory emf) {
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