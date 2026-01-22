package app.giftify.order.application.service;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemConfirmedEvent;
import app.giftify.shared.domain.event.order.OrderPaidEvent;
import app.giftify.shared.domain.event.order.OrderRefundedEvent;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderRepositoryPort orderRepositoryPort;
    private EventPublisher eventPublisher;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepositoryPort = mock(OrderRepositoryPort.class);
        eventPublisher = mock(EventPublisher.class);
        orderService = new OrderService(orderRepositoryPort, eventPublisher);
    }

    @Test
    @DisplayName("Scenario 1: 펀딩 성공 시 주문 생성 및 이벤트 발행")
    void createOrder_Success() {
        // given
        OrderUseCase.CreateOrderCommand command = new OrderUseCase.CreateOrderCommand(
                1L,
                Collections.singletonList(new OrderUseCase.CreateOrderCommand.OrderItemCommand(
                        10L, 100L, 1000L, 1L, Money.of(10000), Quantity.of(1)
                ))
        );

        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-123456")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.save(any(Order.class))).thenReturn(savedOrder);

        // when
        Order result = orderService.createOrder(command);

        // then
        assertNotNull(result);
        assertEquals(OrderStatus.PAYMENT_PENDING, result.getStatus());
        verify(eventPublisher).publish(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("Scenario 4: 결제 성공 시 주문 상태 변경 및 이벤트 발행")
    void payOrder_Success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.findByIdWithLock(orderId)).thenReturn(Optional.of(order));

        // when
        orderService.payOrder(new OrderUseCase.PayOrderCommand(orderId, "payment-key"));

        // then
        assertEquals(OrderStatus.ORDERED, order.getStatus());
        verify(eventPublisher).publish(any(OrderPaidEvent.class));
    }

    @Test
    @DisplayName("Scenario 5 & 2 & 6: 주문 취소/환불 처리 및 이벤트 발행")
    void cancelOrder_Scenario_Test() {
        // [Scenario 5: 결제 환불 시 주문 취소 및 OrderRefundedEvent 발행]
        // given
        Long orderId1 = 1L;
        Order paidOrder = Order.builder()
                .id(orderId1)
                .orderNumber("ORD-PAID")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.ORDERED)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.findByIdWithLock(orderId1)).thenReturn(Optional.of(paidOrder));

        // when
        orderService.cancelOrder(new OrderUseCase.CancelOrderCommand(orderId1));

        // then
        assertEquals(OrderStatus.CANCELED, paidOrder.getStatus());
        verify(eventPublisher).publish(any(OrderRefundedEvent.class));

        // [Scenario 2 & 6: 결제 대기 중 취소 시 OrderCanceledEvent 발행]
        // given
        Long orderId2 = 2L;
        Order pendingOrder = Order.builder()
                .id(orderId2)
                .orderNumber("ORD-PENDING")
                .buyerId(1L)
                .totalAmount(Money.of(10000))
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepositoryPort.findByIdWithLock(orderId2)).thenReturn(Optional.of(pendingOrder));

        // when
        orderService.cancelOrder(new OrderUseCase.CancelOrderCommand(orderId2));

        // then
        assertEquals(OrderStatus.CANCELED, pendingOrder.getStatus());
        verify(eventPublisher).publish(any(OrderCanceledEvent.class));
    }

    @Test
    @DisplayName("이미 취소된 주문은 결제할 수 없다")
    void payOrder_Fail_WhenCanceled() {
        // given
        Long orderId = 1L;
        Order canceledOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.CANCELED)
                .orderNumber("ORD-123456")
                .build();

        when(orderRepositoryPort.findByIdWithLock(orderId)).thenReturn(Optional.of(canceledOrder));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                orderService.payOrder(new OrderUseCase.PayOrderCommand(orderId, "pk"))
        );
    }

    @Test
    @DisplayName("수령자 본인만 주문 아이템을 확정할 수 있다")
    void confirmOrderItem_Success() {
        // given
        Long orderId = 1L;
        Long orderItemId = 10L;
        Long receiverId = 5L;

        app.giftify.order.domain.domain.OrderItem item = app.giftify.order.domain.domain.OrderItem.builder()
                .id(orderItemId)
                .receiverId(receiverId)
                .status(OrderStatus.ORDERED)
                .sellerId(100L)
                .price(Money.of(1000))
                .quantity(Quantity.of(1))
                .build();

        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .status(OrderStatus.ORDERED)
                .orderItems(java.util.List.of(item))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        // when
        orderService.confirmOrderItem(new OrderUseCase.ConfirmOrderItemCommand(orderId, orderItemId, receiverId));

        // then
        assertEquals(OrderStatus.CONFIRMED, item.getStatus());
        verify(eventPublisher).publish(any(OrderItemConfirmedEvent.class));
    }

    @Test
    @DisplayName("수령자가 아닌 사람이 확정을 시도하면 예외가 발생한다")
    void confirmOrderItem_Fail_WrongReceiver() {
        // given
        Long orderId = 1L;
        Long orderItemId = 10L;
        Long receiverId = 5L;
        Long wrongReceiverId = 999L;

        app.giftify.order.domain.domain.OrderItem item = app.giftify.order.domain.domain.OrderItem.builder()
                .id(orderItemId)
                .receiverId(receiverId)
                .status(OrderStatus.ORDERED)
                .build();

        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123456")
                .orderItems(java.util.List.of(item))
                .build();

        when(orderRepositoryPort.findById(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                orderService.confirmOrderItem(new OrderUseCase.ConfirmOrderItemCommand(orderId, orderItemId, wrongReceiverId))
        );
    }
}
