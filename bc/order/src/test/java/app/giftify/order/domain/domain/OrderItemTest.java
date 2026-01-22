package app.giftify.order.domain.domain;

import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    @DisplayName("주문 아이템을 정상적으로 생성한다")
    void createOrderItem_Success() {
        // given
        Money price = Money.of(10000);
        Quantity quantity = Quantity.of(2);

        // when
        OrderItem item = OrderItem.builder()
                .fundingId(1L)
                .productId(2L)
                .sellerId(3L)
                .receiverId(4L)
                .price(price)
                .quantity(quantity)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        // then
        assertEquals(1L, item.getFundingId());
        assertEquals(2L, item.getProductId());
        assertEquals(3L, item.getSellerId());
        assertEquals(4L, item.getReceiverId());
        assertEquals(price, item.getPrice());
        assertEquals(quantity, item.getQuantity());
        assertEquals(OrderStatus.PAYMENT_PENDING, item.getStatus());
    }

    @Test
    @DisplayName("PAYMENT_PENDING 상태에서만 ORDERED로 변경 가능하다")
    void toOrdered_Success() {
        // given
        OrderItem item = createItem(OrderStatus.PAYMENT_PENDING);

        // when
        item.toOrdered();

        // then
        assertEquals(OrderStatus.ORDERED, item.getStatus());
    }

    @Test
    @DisplayName("PAYMENT_PENDING이 아닌 상태에서 toOrdered 호출 시 예외가 발생한다")
    void toOrdered_Fail() {
        // given
        OrderItem item = createItem(OrderStatus.ORDERED);

        // when & then
        assertThrows(IllegalStateException.class, item::toOrdered);
    }

    @Test
    @DisplayName("ORDERED 상태에서만 CONFIRMED로 변경 가능하다")
    void toConfirmed_Success() {
        // given
        OrderItem item = createItem(OrderStatus.ORDERED);

        // when
        item.toConfirmed();

        // then
        assertEquals(OrderStatus.CONFIRMED, item.getStatus());
        assertNotNull(item.getConfirmedAt());
    }

    @Test
    @DisplayName("ORDERED가 아닌 상태에서 toConfirmed 호출 시 예외가 발생한다")
    void toConfirmed_Fail() {
        // given
        OrderItem item = createItem(OrderStatus.PAYMENT_PENDING);

        // when & then
        assertThrows(IllegalStateException.class, item::toConfirmed);
    }

    @Test
    @DisplayName("CONFIRMED 상태가 아니면 취소 가능하다")
    void toCancelled_Success() {
        // given
        OrderItem item = createItem(OrderStatus.ORDERED);

        // when
        item.toCancelled();

        // then
        assertEquals(OrderStatus.CANCELED, item.getStatus());
        assertNotNull(item.getCancelledAt());
    }

    @Test
    @DisplayName("이미 CONFIRMED된 아이템은 취소할 수 없다")
    void toCancelled_Fail() {
        // given
        OrderItem item = createItem(OrderStatus.ORDERED);
        item.toConfirmed();

        // when & then
        assertThrows(IllegalStateException.class, item::toCancelled);
    }

    private OrderItem createItem(OrderStatus status) {
        return OrderItem.builder()
                .price(Money.of(1000))
                .quantity(Quantity.of(1))
                .status(status)
                .build();
    }
}
