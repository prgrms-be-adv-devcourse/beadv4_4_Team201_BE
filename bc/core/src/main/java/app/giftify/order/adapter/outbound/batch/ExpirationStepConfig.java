package app.giftify.order.adapter.outbound.batch;

import app.giftify.order.application.OrderService;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExpirationStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Bean
    @JobScope
    public Step expirationStep(Tasklet expirationTasklet) {
        return new StepBuilder("expirationStep", jobRepository)
                .tasklet(expirationTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet expirationTasklet(
            @Value("#{jobParameters['thresholdMinutes'] ?: 30}") Long thresholdMinutes
    ) {
        return (contribution, chunkContext) -> {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(thresholdMinutes);
            List<Long> orderIds = orderRepository.getIdsByStatusAndCreatedAtBefore(OrderStatus.CREATED, threshold);

            log.info("[주문 만료 배치] 대상 건수: {}건", orderIds.size());

            for (Long id : orderIds) {
                try {
                    orderService.expire(id);
                    contribution.incrementWriteCount(1);
                } catch (Exception e) {
                    log.error("[주문 만료 실패] ID: {}, 사유: {}", id, e.getMessage());
                }
            }

            return RepeatStatus.FINISHED;
        };
    }
}
