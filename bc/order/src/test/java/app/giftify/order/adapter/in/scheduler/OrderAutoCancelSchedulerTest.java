package app.giftify.order.adapter.in.scheduler;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderAutoCancelSchedulerTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderAutoCancelScheduler scheduler;

    @Test
    @DisplayName("만료된 주문들을 찾아 자동으로 취소 처리를 요청한다")
    void autoCancelPendingOrders_Success() {
        // given
        Order order1 = Order.builder().id(1L).orderNumber("ORD-SUCCESS-1").status(OrderStatus.PAYMENT_PENDING).build();
        Order order2 = Order.builder().id(2L).orderNumber("ORD-SUCCESS-2").status(OrderStatus.PAYMENT_PENDING).build();
        
        when(orderRepositoryPort.findPaymentPendingOrdersOlderThan(30)).thenReturn(List.of(order1, order2));

        // when
        scheduler.autoCancelPendingOrders();

        // then
        verify(orderUseCase, times(2)).cancelOrder(any(OrderUseCase.CancelOrderCommand.class));
    }

    @Test
    @DisplayName("개별 주문 취소 중 예외가 발생해도 다른 주문 처리에 영향을 주지 않아야 한다")
    void autoCancelPendingOrders_Exception_Continue() {
        // given
        Order order1 = Order.builder().id(1L).orderNumber("ORD-ERROR-1").status(OrderStatus.PAYMENT_PENDING).build();
        Order order2 = Order.builder().id(2L).orderNumber("ORD-ERROR-2").status(OrderStatus.PAYMENT_PENDING).build();

        when(orderRepositoryPort.findPaymentPendingOrdersOlderThan(30)).thenReturn(List.of(order1, order2));
        doThrow(new RuntimeException("Error")).when(orderUseCase).cancelOrder(argThat(cmd -> cmd.orderId().equals(1L)));

        // when
        scheduler.autoCancelPendingOrders();

        // then
        verify(orderUseCase).cancelOrder(argThat(cmd -> cmd.orderId().equals(1L)));
        verify(orderUseCase).cancelOrder(argThat(cmd -> cmd.orderId().equals(2L)));
    }
}
