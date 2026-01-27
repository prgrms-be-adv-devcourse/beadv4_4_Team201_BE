package app.giftify.funding.domain;

import app.giftify.shared.domain.type.TargetType;
import app.giftify.funding.domain.vo.Money;
import app.giftify.funding.domain.vo.Quantity;
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
        assertThat(orderItem.getCreatedAt()).isEqualTo(now);
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
                .createdAt(LocalDateTime.now())
                .build();
    }
}
