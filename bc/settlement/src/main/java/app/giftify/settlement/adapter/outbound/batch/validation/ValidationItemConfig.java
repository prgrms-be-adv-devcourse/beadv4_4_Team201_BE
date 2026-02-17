package app.giftify.settlement.adapter.outbound.batch.validation;

import app.giftify.settlement.adapter.outbound.batch.common.BatchProperties;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementQueue;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ValidationItemConfig {

    private final EntityManagerFactory emf;
    private final BatchProperties properties;

    @Bean
    @StepScope
    public JpaCursorItemReader<SettlementItem> settlementItemReader(
            @Value("#{stepExecutionContext['minOrderId']}") Long minOrderId,
            @Value("#{stepExecutionContext['maxOrderId']}") Long maxOrderId,
            @Value("#{jobParameters['cutOffDateTime']}") LocalDateTime cutOffDateTime
    ) {
        return new JpaCursorItemReaderBuilder<SettlementItem>()
                .name("settlementItemReader")
                .entityManagerFactory(emf)
                .queryString("""
                    SELECT s FROM SettlementItem s
                    WHERE s.orderId BETWEEN :minOrderId AND :maxOrderId
                      AND (s.statusInfo.status = 'PENDING'
                           OR (s.statusInfo.status = 'FAIL' AND s.retryCount < :retryLimit))
                      AND s.createdAt < :cutOffDateTime
                    ORDER BY s.orderId ASC
                """)
                .parameterValues(Map.of(
                        "minOrderId", minOrderId,
                        "maxOrderId", maxOrderId,
                        "retryLimit", properties.retryLimit(),
                        "cutOffDateTime", cutOffDateTime
                ))
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
}
