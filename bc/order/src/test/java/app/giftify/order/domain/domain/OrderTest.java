package app.giftify.order.domain.domain;

import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 총 금액이 1000원 미만이면 예외가 발생한다")
    void createOrder_MinimumAmount_Exception() {
        Money lowAmount = Money.of(500);
        List<OrderItem> items = new ArrayList<>();
        
        assertThrows(IllegalArgumentException.class, () -> 
            Order.builder()
                    .orderNumber(Order.generateOrderNumber())
                    .buyerId(1L)
                    .totalAmount(lowAmount)
                    .status(OrderStatus.PAYMENT_PENDING)
                    .orderItems(items)
                    .build()
        );
    }

    @Test
    @DisplayName("결제 성공 시 주문과 모든 아이템의 상태가 ORDERED로 변경된다")
    void toOrdered_Success() {
        // given
        OrderItem item = OrderItem.builder()
                .fundingId(1L)
                .productId(1L)
                .sellerId(1L)
                .receiverId(1L)
                .price(Money.of(1000))
                .quantity(Quantity.of(1))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(item);
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(1000))
                .status(OrderStatus.PAYMENT_PENDING)
                .orderItems(items)
                .build();

        // when
        order.toOrdered("payment-key");

        // then
        assertEquals(OrderStatus.ORDERED, order.getStatus());
        assertEquals("payment-key", order.getPaymentKey());
        assertEquals(OrderStatus.ORDERED, item.getStatus());
    }

    @Test
    @DisplayName("모든 아이템이 확정되면 주문 상태도 CONFIRMED로 변경된다 (상향식 전이)")
    void checkAllItemsConfirmed_Success() {
        // given
        OrderItem item1 = OrderItem.builder()
                .fundingId(1L)
                .productId(1L)
                .sellerId(1L)
                .receiverId(1L)
                .price(Money.of(1000))
                .quantity(Quantity.of(1))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        OrderItem item2 = OrderItem.builder()
                .fundingId(1L)
                .productId(1L)
                .sellerId(1L)
                .receiverId(1L)
                .price(Money.of(1000))
                .quantity(Quantity.of(1))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        List<OrderItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(2000))
                .status(OrderStatus.PAYMENT_PENDING)
                .orderItems(items)
                .build();
        order.toOrdered("pk");

        // when
        item1.toConfirmed();
        order.checkAllItemsConfirmed();
        
        // then (아직 하나 남음)
        assertEquals(OrderStatus.ORDERED, order.getStatus());

        // when
        item2.toConfirmed();
        order.checkAllItemsConfirmed();

        // then (모두 확정)
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getConfirmedAt());
    }

    @Test
    @DisplayName("주문 번호 생성 시 6~64자 사이여야 하며 형식을 준수해야 한다")
    void generateOrderNumber_Length_Success() {
        String orderNumber = Order.generateOrderNumber();
        assertTrue(orderNumber.length() >= 6 && orderNumber.length() <= 64);
        assertTrue(orderNumber.matches("^[a-zA-Z0-9-_]{6,64}$"));
    }

    @Test
    @DisplayName("결제 완료 상태가 아닌 경우 결제 처리를 시도하면 예외가 발생한다")
    void toOrdered_Fail_WhenNotPending() {
        // given
        Order order = createBaseOrder(OrderStatus.ORDERED);
        
        // when & then
        assertThrows(IllegalStateException.class, () -> order.toOrdered("pk"));
    }

    @Test
    @DisplayName("모든 아이템이 취소되면 주문 상태도 CANCELED로 변경된다 (상향식 전이)")
    void checkAllItemsCancelled_Success() {
        // given
        OrderItem item1 = OrderItem.builder()
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        OrderItem item2 = OrderItem.builder()
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(2000))
                .status(OrderStatus.PAYMENT_PENDING)
                .orderItems(List.of(item1, item2))
                .build();

        // when
        item1.toCancelled();
        order.checkAllItemsCancelled();
        
        // then (아직 하나 남음)
        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());

        // when
        item2.toCancelled();
        order.checkAllItemsCancelled();

        // then (모두 취소)
        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertNotNull(order.getCancelledAt());
    }

    @Test
    @DisplayName("자동 취소 가능 여부를 정확히 판단한다")
    void canAutoCancel_Success() {
        // given
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(31))
                .build();

        // when
        boolean canCancel = order.canAutoCancel(30);

        // then
        assertTrue(canCancel);
    }

    private Order createBaseOrder(OrderStatus status) {
        return Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(1000))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
