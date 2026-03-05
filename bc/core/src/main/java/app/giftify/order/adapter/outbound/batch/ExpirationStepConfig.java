package app.giftify.order.adapter.outbound.batch;

import app.giftify.order.application.OrderService;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
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
    public Step expirationStep() {
        return new StepBuilder("expirationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDateTime threshold = LocalDateTime.now().minusMinutes(30L);
                    List<Long> orderIds = orderRepository.getIdsByStatusAndCreatedAtBefore(OrderStatus.CREATED, threshold);

                    for (Long id : orderIds) {
                        try {
                            contribution.incrementReadCount();

                            orderService.expire(id);

                            contribution.incrementWriteCount(1);
                        } catch (Exception e) {
                            chunkContext.getStepContext().getStepExecution().addFailureException(e);
                            log.error("주문 만료 실패 - ID: {}, 사유: {}", id, e.getMessage());
                        }
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
