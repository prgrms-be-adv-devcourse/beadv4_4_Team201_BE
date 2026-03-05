package app.giftify.order.adapter.outbound.batch;

import app.giftify.order.application.OrderService;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.OrderStatus;
import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBatchTest
@SpringBootTest(
        classes = {
                ExpirationJobConfig.class,
                ExpirationStepConfig.class,
                ExpirationJobListener.class,
                ExpirationJobTest.BatchAutoConfig.class
        },
        properties = "spring.batch.job.enabled=false"
)
class ExpirationJobTest {

    @EnableAutoConfiguration(exclude = {
            SecurityAutoConfiguration.class,
            SecurityFilterAutoConfiguration.class,
            OAuth2ResourceServerAutoConfiguration.class,
            SharedSecurityAutoConfiguration.class
    })
    static class BatchAutoConfig {
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("성공: 만료 대상 주문이 있으면 COMPLETED로 완료되고 만료 처리를 수행한다")
    void expirationJob_withExpiredOrders_completedAndProcessed() throws Exception {
        // given
        List<Long> expiredOrderIds = List.of(1L, 2L, 3L);
        given(orderRepository.getIdsByStatusAndCreatedAtBefore(eq(OrderStatus.CREATED), any(LocalDateTime.class)))
                .willReturn(expiredOrderIds);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(orderService, times(3)).expire(any(Long.class));
    }

    @Test
    @DisplayName("성공: 만료 대상 주문이 없으면 COMPLETED로 완료되고 만료 처리는 호출되지 않는다")
    void expirationJob_withNoExpiredOrders_completedWithoutProcessing() throws Exception {
        // given
        given(orderRepository.getIdsByStatusAndCreatedAtBefore(eq(OrderStatus.CREATED), any(LocalDateTime.class)))
                .willReturn(List.of());

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(orderService, never()).expire(any(Long.class));
    }
}