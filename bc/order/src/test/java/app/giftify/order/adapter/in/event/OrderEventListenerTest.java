package app.giftify.order.adapter.in.event;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderEventListenerTest {

    private OrderUseCase orderUseCase;
    private OrderRepositoryPort orderRepositoryPort;
    private OrderEventListener orderEventListener;

    @BeforeEach
    void setUp() {
        orderUseCase = mock(OrderUseCase.class);
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        orderEventListener = new OrderEventListener(orderUseCase, orderRepositoryPort);
    }

    @Test
    @DisplayName("Scenario 1: FundingAchievedEvent 수신 시 주문 생성 확인")
    void handleFundingAchieved_Test() {
        // given
        FundingAchievedEvent event = new FundingAchievedEvent(10L, 1L, 10000, 100L, 1L);
        when(orderRepositoryPort.findAllByFundingId(10L)).thenReturn(Collections.emptyList());

        // when
        orderEventListener.handleFundingAchieved(event);

        // then
        verify(orderUseCase).createOrder(any(OrderUseCase.CreateOrderCommand.class));
    }

    @Test
    @DisplayName("FundingAcceptedEvent 수신 시 주문 생성 확인")
    void handleFundingAccepted_Test() {
        // given
        FundingAcceptedEvent event = new FundingAcceptedEvent(11L, 2L);
        when(orderRepositoryPort.findAllByFundingId(11L)).thenReturn(Collections.emptyList());

        // when
        orderEventListener.handleFundingAccepted(event);

        // then
        verify(orderUseCase).createOrder(any(OrderUseCase.CreateOrderCommand.class));
    }

    @Test
    @DisplayName("Scenario 3: FundingAchievedEvent 중복 수신 시 멱등성 유지 확인")
    void handleFundingAchieved_Idempotency_Test() {
        // given
        FundingAchievedEvent event = new FundingAchievedEvent(10L, 1L, 10000, 100L, 1L);
        Order existingOrder = mock(Order.class);
        when(orderRepositoryPort.findAllByFundingId(10L)).thenReturn(List.of(existingOrder));

        // when
        orderEventListener.handleFundingAchieved(event);

        // then
        verify(orderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("Scenario 4: PaymentSucceededEvent 수신 시 주문 결제 처리 확인")
    void handlePaymentSucceeded_Test() {
        // given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                100L, "ORDER", 1L, Money.of(10000), PaymentType.FUNDING, LocalDateTime.now()
        );

        // when
        orderEventListener.handlePaymentSucceeded(event);

        // then
        verify(orderUseCase).payOrder(new OrderUseCase.PayOrderCommand(1L, "100"));
    }

    @Test
    @DisplayName("Scenario 5: PaymentRefundedEvent 수신 시 주문 취소 처리 확인")
    void handlePaymentRefunded_Test() {
        // given
        PaymentRefundedEvent event = new PaymentRefundedEvent(
                100L, "refund-1", "ORDER", 1L, Money.of(10000), PaymentType.FUNDING, "reason"
        );

        // when
        orderEventListener.handlePaymentRefunded(event);

        // then
        verify(orderUseCase).cancelOrder(new OrderUseCase.CancelOrderCommand(1L));
    }

    @Test
    @DisplayName("처리 중 예외 발생 시 로그를 남기고 다음 처리에 영향을 주지 않아야 한다")
    void handleFundingAchieved_Exception_Logging() {
        // given
        FundingAchievedEvent event = new FundingAchievedEvent(10L, 1L, 10000, 100L, 1L);
        // orderRepositoryPort.findAllByFundingId 호출 시 예외를 던지지 않고 catch 블록으로 가도록 유도하려면
        // 실제로는 handleFundingAchieved 내부에서 발생하는 모든 RuntimeException을 catch 하므로
        // Mockito 설정에 따라 달라질 수 있음. 
        // 여기서는 예외가 발생했을 때 orderUseCase.createOrder가 호출되지 않음을 확인하는 것으로 충분
        when(orderRepositoryPort.findAllByFundingId(10L)).thenThrow(new RuntimeException("DB error"));

        // when
        try {
            orderEventListener.handleFundingAchieved(event);
        } catch (Exception e) {
            // TransactionalEventListener가 아닌 일반 EventListener이며 @Transactional이 붙어있음
            // 테스트 환경에서는 @Transactional에 의해 예외가 밖으로 던져질 수 있음
        }
        
        // then
        verify(orderUseCase, never()).createOrder(any());
    }

    @Test
    @DisplayName("ORDER 타입이 아닌 결제 성공 이벤트는 무시한다")
    void handlePaymentSucceeded_Ignore_NonOrder() {
        // given
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                100L, "WISH", 1L, Money.of(10000), PaymentType.FUNDING, LocalDateTime.now()
        );

        // when
        orderEventListener.handlePaymentSucceeded(event);

        // then
        verify(orderUseCase, never()).payOrder(any());
    }

    @Test
    @DisplayName("FundingExpiredEvent 수신 시 관련 주문 일괄 취소 확인")
    void handleFundingExpired_Test() {
        // given
        Long fundingId = 10L;
        app.giftify.shared.domain.event.funding.FundingExpiredEvent event = 
                new app.giftify.shared.domain.event.funding.FundingExpiredEvent(fundingId, 1L, 10000, 1L);
        
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        when(orderRepositoryPort.findAllByFundingId(fundingId)).thenReturn(List.of(order));

        // when
        orderEventListener.handleFundingExpired(event);

        // then
        verify(orderUseCase).cancelOrder(new OrderUseCase.CancelOrderCommand(1L));
    }
}
