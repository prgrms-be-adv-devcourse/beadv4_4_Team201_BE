package app.giftify.order.adapter.inbound.event;

import app.giftify.order.application.FundingOrderService;
import app.giftify.order.application.OrderService;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentEventData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(classes = {
        OrderEventListener.class,           // 1. 테스트 대상
        OrderEventListenerTest.TestConfig.class // 2. 테스트 설정 (Retry 활성화)
})
class OrderEventListenerTest {

    @TestConfiguration
    @EnableRetry
    @Import(OrderEventListener.class)
    static class TestConfig {
        // 테스트에 필요한 공통 설정이 있다면 여기에 작성
    }

    @Autowired
    private OrderEventListener orderEventListener;

    @MockitoBean
    private OrderService orderService; // 의존성 모킹

    @MockitoBean
    private FundingOrderService fundingOrderService;

    @Test
    @DisplayName("재시도 가능한 에러코드의 InfraException 발생 시 recover가 호출되어야 한다")
    void recover_test_with_enum_retryable() {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.create(
                mock(PaymentEventData.class)
        );

        // 1. 재시도가 가능한 Enum 값을 가진 실제 Exception 생성
        // 예: ErrorCode.NETWORK_TIMEOUT.isRetryable() == true 라고 가정
        InfraException retryableException = new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT);

        // 2. 서비스 호출 시 해당 예외를 던지도록 설정
        willThrow(retryableException)
                .given(orderService).completeCancel(any());

        // when & then
        assertThatThrownBy(() -> orderEventListener.on(event))
                .isInstanceOf(InfraException.class);

        // then: 실제로 재시도가 발생했는지 횟수 확인
        verify(orderService, atLeast(2)).completeCancel(any());
    }
}