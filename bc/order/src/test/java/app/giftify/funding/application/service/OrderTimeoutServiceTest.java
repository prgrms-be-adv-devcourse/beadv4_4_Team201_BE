package app.giftify.funding.application.service;

import app.giftify.funding.application.outbound.OrderItemRepositoryPort;
import app.giftify.funding.application.outbound.OrderNotificationPort;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderItem;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.funding.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutServiceTest {

    @InjectMocks
    private OrderTimeoutService orderTimeoutService;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderItemRepositoryPort orderItemRepositoryPort;

    @Mock
    private OrderNotificationPort orderNotificationPort;

    @Test
    @DisplayName("10분이 지난 결제 대기 상태의 주문들을 조회하여 알림을 보내고 삭제한다")
    void handleTimedOutOrders_success() {
        // given
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-TIMEOUT-001")
                .buyerId(100L)
                .totalAmount(Money.of(10000))
                .paymentMethod(PaymentMethod.CARD)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(11))
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .build();

        given(orderRepositoryPort.findByStatusAndCreatedAtBefore(eq(OrderStatus.PAYMENT_PENDING), any(LocalDateTime.class)))
                .willReturn(List.of(order));
        given(orderItemRepositoryPort.findByOrderId(1L)).willReturn(List.of(item));

        // when
        orderTimeoutService.handleTimedOutOrders();

        // then
        verify(orderNotificationPort).notifyOrderTimeout(order);
        verify(orderItemRepositoryPort).deleteAll(List.of(item));
        verify(orderRepositoryPort).delete(order);
    }

    @Test
    @DisplayName("만료된 주문이 없으면 아무 작업도 하지 않는다")
    void handleTimedOutOrders_noOrders() {
        // given
        given(orderRepositoryPort.findByStatusAndCreatedAtBefore(eq(OrderStatus.PAYMENT_PENDING), any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        orderTimeoutService.handleTimedOutOrders();

        // then
        verifyNoInteractions(orderNotificationPort);
        verify(orderItemRepositoryPort, never()).findByOrderId(anyLong());
        verify(orderRepositoryPort, never()).delete(any());
    }
}
