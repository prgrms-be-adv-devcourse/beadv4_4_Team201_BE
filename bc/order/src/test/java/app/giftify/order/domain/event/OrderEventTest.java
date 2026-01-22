package app.giftify.order.domain.event;

import app.giftify.shared.domain.event.order.*;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderEventTest {

    @Test
    @DisplayName("OrderCreatedEvent 필드 검증")
    void orderCreatedEvent_Test() {
        OrderCreatedEvent.OrderItemInfo itemInfo = new OrderCreatedEvent.OrderItemInfo(1L, 10L, 100L, 1000L);
        OrderCreatedEvent event = new OrderCreatedEvent(1L, "ORD-1", 2L, List.of(itemInfo));

        assertEquals(1L, event.getOrderId());
        assertEquals("ORD-1", event.getOrderNumber());
        assertEquals(2L, event.getBuyerId());
        assertEquals(1, event.getItems().size());
        assertEquals(1L, event.getItems().get(0).orderItemId());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("OrderPaidEvent 필드 검증")
    void orderPaidEvent_Test() {
        OrderPaidEvent event = new OrderPaidEvent(1L, "ORD-1", "PAY-KEY");

        assertEquals(1L, event.getOrderId());
        assertEquals("ORD-1", event.getOrderNumber());
        assertEquals("PAY-KEY", event.getPaymentKey());
    }

    @Test
    @DisplayName("OrderItemConfirmedEvent 필드 검증")
    void orderItemConfirmedEvent_Test() {
        Money price = Money.of(1000);
        OrderItemConfirmedEvent event = new OrderItemConfirmedEvent(1L, 2L, 3L, 4L, price, 1);

        assertEquals(1L, event.getOrderId());
        assertEquals(2L, event.getOrderItemId());
        assertEquals(3L, event.getSellerId());
        assertEquals(4L, event.getReceiverId());
        assertEquals(price, event.getPrice());
        assertEquals(1, event.getQuantity());
    }

    @Test
    @DisplayName("OrderCanceledEvent 필드 검증")
    void orderCanceledEvent_Test() {
        OrderCanceledEvent event = new OrderCanceledEvent(1L, "ORD-1");
        assertEquals(1L, event.getOrderId());
        assertEquals("ORD-1", event.getOrderNumber());
    }

    @Test
    @DisplayName("OrderRefundedEvent 필드 검증")
    void orderRefundedEvent_Test() {
        OrderRefundedEvent event = new OrderRefundedEvent(1L, "ORD-1");
        assertEquals(1L, event.getOrderId());
        assertEquals("ORD-1", event.getOrderNumber());
    }
}
