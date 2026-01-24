package app.giftify.funding.domain;

import app.giftify.funding.domain.exception.OrderException;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    @DisplayName("주문 아이템 생성 시 초기 상태 및 필드 값이 올바르게 설정된다")
    void createOrderItem() {
        // given
        Long orderId = 1L;
        Long targetSnapshotId = 100L;
        TargetType targetType = TargetType.FUNDING;
        Long sellerId = 2L;
        Long receiverId = 3L;
        Money price = Money.of(10000);
        Quantity quantity = new Quantity(1);
        LocalDateTime now = LocalDateTime.now();

        // when
        OrderItem orderItem = OrderItem.builder()
                .orderId(orderId)
                .targetSnapshotId(targetSnapshotId)
                .targetType(targetType)
                .sellerId(sellerId)
                .receiverId(receiverId)
                .price(price)
                .quantity(quantity)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(now)
                .build();

        // then
        assertThat(orderItem.getOrderId()).isEqualTo(orderId);
        assertThat(orderItem.getTargetSnapshotId()).isEqualTo(targetSnapshotId);
        assertThat(orderItem.getTargetType()).isEqualTo(targetType);
        assertThat(orderItem.getSellerId()).isEqualTo(sellerId);
        assertThat(orderItem.getReceiverId()).isEqualTo(receiverId);
        assertThat(orderItem.getPrice()).isEqualTo(price);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(orderItem.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("주문 대기 상태에서 결제 완료 상태로 변경할 수 있다")
    void toOrdered() {
        // given
        OrderItem orderItem = createPendingOrderItem();

        // when
        orderItem.toOrdered();

        // then
        assertThat(orderItem.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    @DisplayName("주문 대기 상태가 아닐 때 결제 완료 처리를 시도하면 예외가 발생한다")
    void toOrdered_fail_invalidStatus() {
        // given
        OrderItem orderItem = createPendingOrderItem();
        orderItem.toOrdered();
        orderItem.toConfirmed();

        // when & then
        assertThatThrownBy(orderItem::toOrdered)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 대기 상태에서만 결제 완료로 변경 가능합니다.");
    }

    @Test
    @DisplayName("주문 결제 완료 상태에서 주문 확정 상태로 변경할 수 있다")
    void toConfirmed() {
        // given
        OrderItem orderItem = createPendingOrderItem();
        orderItem.toOrdered();

        // when
        orderItem.toConfirmed();

        // then
        assertThat(orderItem.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(orderItem.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 결제 완료 상태가 아닐 때 주문 확정 처리를 시도하면 예외가 발생한다")
    void toConfirmed_fail_invalidStatus() {
        // given
        OrderItem orderItem = createPendingOrderItem();

        // when & then
        assertThatThrownBy(orderItem::toConfirmed)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("주문 결제 완료 상태에서만 확정이 가능합니다.");
    }

    @Test
    @DisplayName("확정되지 않은 주문 아이템은 취소할 수 있다")
    void toCancelled() {
        // given
        OrderItem orderItem = createPendingOrderItem();

        // when
        orderItem.toCancelled();

        // then
        assertThat(orderItem.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(orderItem.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 확정된 주문 아이템은 취소할 수 없으며 예외가 발생한다")
    void toCancelled_fail_alreadyConfirmed() {
        // given
        OrderItem orderItem = createPendingOrderItem();
        orderItem.toOrdered();
        orderItem.toConfirmed();

        // when & then
        assertThatThrownBy(orderItem::toCancelled)
                .isInstanceOf(OrderException.class)
                .hasMessageContaining("이미 확정된 주문 아이템은 취소할 수 없습니다.");
    }

    private OrderItem createPendingOrderItem() {
        return OrderItem.builder()
                .orderId(1L)
                .targetSnapshotId(100L)
                .targetType(TargetType.FUNDING)
                .sellerId(2L)
                .receiverId(3L)
                .price(Money.of(10000))
                .quantity(new Quantity(1))
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
