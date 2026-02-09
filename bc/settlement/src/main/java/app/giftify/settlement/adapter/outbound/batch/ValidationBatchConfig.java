package app.giftify.settlement.adapter.outbound.batch;

import app.giftify.settlement.domain.SettlementItem;
import app.giftify.settlement.domain.SettlementQueue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ValidationBatchConfig {

    @Value("${settlement.batch.chunk-size}")
    private int chunkSize;
    @Value("${settlement.batch.max-retry-count}")
    private int maxRetryCount;

    @Value("#{jobParameters['cutOffDateTime']}")
    private LocalDateTime cutOffDateTime;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final OrderIdPartitioner orderIdPartitioner;
    private final ValidationBulkLoadListener validationBulkLoadListener;

    @Bean
    public Job validationJob() {
        return new JobBuilder("validationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(validationPartitionStep())
                .next(cleanupSettlementQueueStep(emf))
                .build();
    }

    @Bean
    public Step validationStep() {
        return new StepBuilder("validationStep", jobRepository)
                .<SettlementItem, SettlementQueue>chunk(chunkSize, transactionManager)
                .reader(settlementItemReader(null))
                .processor(validationProcessor(null))
                .writer(validationWriter())
                .listener(validationBulkLoadListener)
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
            @Value("#{stepExecutionContext['orderIds']}") List<Long> orderIds
    ) {
        return new JpaPagingItemReaderBuilder<SettlementItem>()
                .name("settlementItemReader")
                .entityManagerFactory(emf)
                .queryString("""
                    SELECT s FROM SettlementItem s
                    WHERE (s.lifeCycleMeta.status = 'PENDING'
                           OR (s.lifeCycleMeta.status = 'FAIL' AND s.retryCount < :maxRetryCount))
                      AND s.createdAt < :cutOffDateTime
                """)
                .parameterValues(Map.of(
                        "orderIds", orderIds,
                        "maxRetryCount", maxRetryCount
                        , "cutOffDateTime", cutOffDateTime
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
                    EntityManager em = emf.createEntityManager();
                    em.createQuery("DELETE FROM SettlementQueue").executeUpdate();
                    em.close();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}