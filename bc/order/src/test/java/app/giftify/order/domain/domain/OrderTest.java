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
    @DisplayName("확정된 주문은 취소할 수 없다")
    void toCancelled_Fail_WhenConfirmed() {
        // given
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(1L)
                .totalAmount(Money.of(1000))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        order.toOrdered("pk");
        order.toConfirmed();

        // when & then
        assertThrows(IllegalStateException.class, order::toCancelled);
    }
}
