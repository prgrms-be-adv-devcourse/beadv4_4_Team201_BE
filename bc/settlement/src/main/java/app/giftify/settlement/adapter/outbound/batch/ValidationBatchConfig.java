package app.giftify.settlement.adapter.outbound.batch;

import app.giftify.settlement.domain.SettlementItem;
import app.giftify.settlement.domain.SettlementItemStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ValidationBatchConfig {

    @Value("${settlement.batch.chunk-size}")
    private int chunkSize;
    @Value("${settlement.batch.max-retry-count}")
    private int retryCount;
    @Value("${settlement.batch.validation-cut-off-time}")
    private String cutOffTimeStr;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;

    @Bean
    public Job validationJob() {
        return new JobBuilder("validationJob", jobRepository)
                .start(validationStep())
                .build();
    }

    @Bean
    public Step validationStep() {
        return new StepBuilder("validationStep", jobRepository)
                .<Long, List<SettlementItem>>chunk(chunkSize, transactionManager)
                .reader(pendingOrderIdReader())
                .processor(item -> List.of())
                .writer(chunk -> {})
                .build();
    }

    /**
     * 1.1. 대상 추출: 01:00 이전 생성된 PENDING 상태의 중복 없는 OrderId 조회
     */
    @Bean
    public JpaPagingItemReader<Long> pendingOrderIdReader() {
        LocalDateTime cutOffDateTime = calculateCutOffDateTime();

        return new JpaPagingItemReaderBuilder<Long>()
                .name("pendingOrderIdReader")
                .entityManagerFactory(emf)
                .queryString("SELECT DISTINCT s.orderId FROM SettlementItem s " +
                             "WHERE s.lifeCycleMeta.status = :status " +
                             "AND s.createdAt < :cutOffDateTime " +
                             "AND s.retryCount < :retryCount " +
                             "ORDER BY s.orderId ASC")
                .parameterValues(Map.of(
                        "status", SettlementItemStatus.PENDING,
                        "cutOffDateTime", cutOffDateTime,
                        "retryCount", retryCount
                ))
                .pageSize(chunkSize)
                .build();
    }

    private @NonNull LocalDateTime calculateCutOffDateTime() {
        LocalTime localTime = LocalTime.parse(cutOffTimeStr);
        return localTime.atDate(LocalDate.now());
    }
}