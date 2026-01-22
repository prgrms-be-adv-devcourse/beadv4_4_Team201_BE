package app.giftify.order.adapter.in.event;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderStatus;
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
    @DisplayName("Scenario 2: FundingCanceledEvent 수신 시 관련 주문 일괄 취소 확인")
    void handleFundingCanceled_Test() {
        // given
        Long fundingId = 10L;
        FundingCanceledEvent event = new FundingCanceledEvent(fundingId, 1L, 10000, 100L, 1L);
        
        Order order1 = mock(Order.class);
        when(order1.getId()).thenReturn(1L);
        Order order2 = mock(Order.class);
        when(order2.getId()).thenReturn(2L);
        
        when(orderRepositoryPort.findAllByFundingId(fundingId)).thenReturn(List.of(order1, order2));

        // when
        orderEventListener.handleFundingCanceled(event);

        // then
        verify(orderUseCase).cancelOrder(new OrderUseCase.CancelOrderCommand(1L));
        verify(orderUseCase).cancelOrder(new OrderUseCase.CancelOrderCommand(2L));
    }
}
