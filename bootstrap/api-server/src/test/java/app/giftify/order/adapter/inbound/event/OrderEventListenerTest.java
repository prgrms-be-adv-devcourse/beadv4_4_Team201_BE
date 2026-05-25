package app.giftify.order.adapter.inbound.event;

import app.giftify.order.application.FundingOrderService;
import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.command.ConfirmFundingOrderCommand;
import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.support.common.api.exception.BusinessException;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.api.exception.InfraErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.order.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.funding.domain.event.FundingCanceledEvent;
import app.giftify.funding.domain.event.FundingConfirmPendingEvent;
import app.giftify.support.common.money.Money;
import app.giftify.order.domain.event.OrderConfirmFailedEvent;
import app.giftify.order.domain.event.OrderConfirmedEvent;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentCancelData;
import app.giftify.payment.domain.event.PaymentSuccessData;
import app.giftify.payment.domain.event.PaymentSucceededEvent;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @MockitoBean
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("재시도 가능한 에러코드의 InfraException 발생 시 recover가 호출되어야 한다")
    void recover_test_with_enum_retryable() {
        // given
        PaymentCanceledEvent event = PaymentCanceledEvent.create(
                mock(PaymentCancelData.class)
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

    @Nested
    @DisplayName("FundingCanceledEvent 처리 (on(FundingCanceledEvent))")
    class OnFundingCanceledEvent {

        private static final Long FUNDING_ID = 1L;
        private static final Long WISHLIST_ITEM_ID = 10L;
        private static final Integer CANCELED_AMOUNT = 50000;
        private static final Long RECEIVER_ID = 100L;

        @Test
        @DisplayName("성공: 정상 처리 후 requestCancelFundingOrder가 호출된다")
        void given_success_when_onFundingCanceledEvent_then_callRequestCancelFundingOrder() {
            // given
            FundingCanceledEvent event = new FundingCanceledEvent(
                    FUNDING_ID, WISHLIST_ITEM_ID, CANCELED_AMOUNT, RECEIVER_ID, List.of(101L, 102L)
            );

            // when
            orderEventListener.on(event);

            // then
            verify(fundingOrderService).requestCancelFundingOrder(any(CancelFundingOrderCommand.class));
        }

        @Test
        @DisplayName("성공: 커맨드에 이벤트의 fundingId와 canceledAmount가 올바르게 전달된다")
        void given_event_when_onFundingCanceledEvent_then_commandHasCorrectData() {
            // given
            FundingCanceledEvent event = new FundingCanceledEvent(
                    FUNDING_ID, WISHLIST_ITEM_ID, CANCELED_AMOUNT, RECEIVER_ID, List.of(101L, 102L)
            );

            // when
            orderEventListener.on(event);

            // then
            ArgumentCaptor<CancelFundingOrderCommand> captor =
                    ArgumentCaptor.forClass(CancelFundingOrderCommand.class);
            verify(fundingOrderService).requestCancelFundingOrder(captor.capture());
            assertThat(captor.getValue().fundingId()).isEqualTo(FUNDING_ID);
            assertThat(captor.getValue().expiredAmount()).isEqualTo(Money.of(CANCELED_AMOUNT));
        }
    }

    @Nested
    @DisplayName("FundingConfirmPendingEvent 처리 (on(FundingConfirmPendingEvent))")
    class OnFundingConfirmPendingEvent {

        private static final Long FUNDING_ID = 1L;
        private static final Long PRODUCT_ID = 100L;

        @Test
        @DisplayName("성공: 정상 처리 후 fundingId를 담은 OrderConfirmedEvent가 발행된다")
        void given_success_when_onFundingConfirmPendingEvent_then_publishOrderConfirmedEvent() {
            // given
            FundingConfirmPendingEvent event = new FundingConfirmPendingEvent(FUNDING_ID, PRODUCT_ID);

            // when
            orderEventListener.on(event);

            // then
            verify(fundingOrderService).confirmOrderItemsByFunding(any(ConfirmFundingOrderCommand.class));
            ArgumentCaptor<OrderConfirmedEvent> captor = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getFundingId()).isEqualTo(FUNDING_ID);
        }

        @Test
        @DisplayName("성공: 커맨드에 이벤트의 fundingId와 productId가 올바르게 전달된다")
        void given_event_when_onFundingConfirmPendingEvent_then_commandHasCorrectIds() {
            // given
            FundingConfirmPendingEvent event = new FundingConfirmPendingEvent(FUNDING_ID, PRODUCT_ID);

            // when
            orderEventListener.on(event);

            // then
            ArgumentCaptor<ConfirmFundingOrderCommand> captor = ArgumentCaptor.forClass(ConfirmFundingOrderCommand.class);
            verify(fundingOrderService).confirmOrderItemsByFunding(captor.capture());
            assertThat(captor.getValue().fundingId()).isEqualTo(FUNDING_ID);
            assertThat(captor.getValue().productId()).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("실패: BusinessException 발생 시 fundingId를 담은 OrderConfirmFailedEvent가 발행된다")
        void given_businessException_when_onFundingConfirmPendingEvent_then_publishOrderConfirmFailedEvent() {
            // given
            FundingConfirmPendingEvent event = new FundingConfirmPendingEvent(FUNDING_ID, PRODUCT_ID);
            doThrow(new DomainException(OrderErrorCode.INVALID_STATUS_TRANSITION))
                    .when(fundingOrderService).confirmOrderItemsByFunding(any(ConfirmFundingOrderCommand.class));

            // when & then
            assertThatThrownBy(() -> orderEventListener.on(event)).isInstanceOf(BusinessException.class);

            // then
            ArgumentCaptor<OrderConfirmFailedEvent> captor = ArgumentCaptor.forClass(OrderConfirmFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getFundingId()).isEqualTo(FUNDING_ID);
        }

        @Test
        @DisplayName("실패: InfraException 발생 시 fundingId를 담은 OrderConfirmFailedEvent가 발행된다")
        void given_infraException_when_onFundingConfirmPendingEvent_then_publishOrderConfirmFailedEvent() {
            // given
            FundingConfirmPendingEvent event = new FundingConfirmPendingEvent(FUNDING_ID, PRODUCT_ID);
            doThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT))
                    .when(fundingOrderService).confirmOrderItemsByFunding(any(ConfirmFundingOrderCommand.class));

            // when & then
            assertThatThrownBy(() -> orderEventListener.on(event)).isInstanceOf(InfraException.class);

            ArgumentCaptor<OrderConfirmFailedEvent> captor = ArgumentCaptor.forClass(OrderConfirmFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getFundingId()).isEqualTo(FUNDING_ID);
        }

        @Test
        @DisplayName("실패: 예상치 못한 예외 발생 시 '예기치 못한 오류 발생' 메시지로 OrderConfirmFailedEvent가 발행된다")
        void given_unexpectedException_when_onFundingConfirmPendingEvent_then_publishFailedEventWithDefaultMessage() {
            // given
            FundingConfirmPendingEvent event = new FundingConfirmPendingEvent(FUNDING_ID, PRODUCT_ID);
            doThrow(new RuntimeException("DB connection lost"))
                    .when(fundingOrderService).confirmOrderItemsByFunding(any(ConfirmFundingOrderCommand.class));

            // when & then
            assertThatThrownBy(() -> orderEventListener.on(event)).isInstanceOf(Exception.class);

            // then
            ArgumentCaptor<OrderConfirmFailedEvent> captor = ArgumentCaptor.forClass(OrderConfirmFailedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getFundingId()).isEqualTo(FUNDING_ID);
            assertThat(captor.getValue().getReason()).isEqualTo("예기치 못한 오류 발생");
        }
    }

    @Nested
    @DisplayName("PaymentSucceededEvent 처리 (on(PaymentSucceededEvent))")
    class OnPaymentSucceededEvent {

        private static final Long PAYMENT_ID = 42L;
        private static final Long ORDER_ID = 100L;
        private static final Long MEMBER_ID = 1L;
        private static final String ORDER_NUMBER = "ORD-100";
        private static final String TRANSACTION_KEY = "tk-1";
        private static final String PAYMENT_KEY = "pk-1";

        private PaymentSucceededEvent eventOf(String orderNumber, Long paymentId, String transactionKey) {
            PaymentSuccessData data = new PaymentSuccessData(
                    paymentId, ORDER_ID, MEMBER_ID, orderNumber,
                    Money.of(10000L),
                    PaymentMethod.CARD, PaymentType.FUNDING,
                    PAYMENT_KEY, transactionKey
            );
            return PaymentSucceededEvent.create(data);
        }

        @Test
        @DisplayName("성공: PaymentSucceededEvent 수신 시 markOrderAsPaid 가 호출된다")
        void on_PaymentSucceededEvent_calls_markOrderAsPaid() {
            // given
            PaymentSucceededEvent event = eventOf(ORDER_NUMBER, PAYMENT_ID, TRANSACTION_KEY);

            // when
            orderEventListener.on(event);

            // then
            ArgumentCaptor<MarkOrderAsPaidCommand> captor = ArgumentCaptor.forClass(MarkOrderAsPaidCommand.class);
            verify(orderService).markOrderAsPaid(captor.capture());
            MarkOrderAsPaidCommand cmd = captor.getValue();
            assertThat(cmd.orderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(cmd.paymentId()).isEqualTo(PAYMENT_ID);
            assertThat(cmd.lastTransactionKey()).isEqualTo(TRANSACTION_KEY);
        }

        @Test
        @DisplayName("재시도: 재시도 가능한 InfraException 발생 시 markOrderAsPaid 가 재호출된다")
        void on_PaymentSucceededEvent_retries_on_retryable_infra_exception() {
            // given
            PaymentSucceededEvent event = eventOf(ORDER_NUMBER, PAYMENT_ID, TRANSACTION_KEY);
            InfraException retryable = new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT);
            willThrow(retryable).given(orderService).markOrderAsPaid(any());

            // when & then
            assertThatThrownBy(() -> orderEventListener.on(event))
                    .isInstanceOf(InfraException.class);
            verify(orderService, atLeast(2)).markOrderAsPaid(any());
        }
    }
}
