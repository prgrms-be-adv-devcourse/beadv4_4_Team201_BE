package app.giftify.funding.adapter.outbound.jpa.mapper;

import app.giftify.funding.adapter.outbound.jpa.entity.OrderItemEntity;
import app.giftify.funding.domain.OrderItem;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    @Test
    @DisplayName("도메인 모델을 엔티티로 변환한다")
    void toEntity() {
        // given
        OrderItem orderItem = OrderItem.builder()
                .orderId(1L)
                .targetSnapshotId(100L)
                .targetType(TargetType.PRODUCT)
                .sellerId(200L)
                .receiverId(300L)
                .price(Money.of(10000L))
                .quantity(new Quantity(2))
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // when
        OrderItemEntity entity = OrderItemMapper.toEntity(orderItem);

        // then
        assertThat(entity.getOrderId()).isEqualTo(orderItem.getOrderId());
        assertThat(entity.getTargetSnapshotId()).isEqualTo(orderItem.getTargetSnapshotId());
        assertThat(entity.getTargetType()).isEqualTo(orderItem.getTargetType());
        assertThat(entity.getSellerId()).isEqualTo(orderItem.getSellerId());
        assertThat(entity.getReceiverId()).isEqualTo(orderItem.getReceiverId());
        assertThat(entity.getPrice()).isEqualByComparingTo(orderItem.getPrice().amount());
        assertThat(entity.getQuantity()).isEqualTo(orderItem.getQuantity().getValue());
        assertThat(entity.getStatus()).isEqualTo(orderItem.getStatus());
    }

    @Test
    @DisplayName("엔티티를 도메인 모델로 변환한다")
    void toDomain() {
        // given
        OrderItemEntity entity = OrderItemEntity.builder()
                .orderId(1L)
                .targetSnapshotId(100L)
                .targetType(TargetType.PRODUCT)
                .sellerId(200L)
                .receiverId(300L)
                .price(java.math.BigDecimal.valueOf(10000))
                .quantity(2)
                .status(OrderStatus.PAYMENT_PENDING)
                .build();

        // when
        OrderItem orderItem = OrderItemMapper.toDomain(entity);

        // then
        assertThat(orderItem.getOrderId()).isEqualTo(entity.getOrderId());
        assertThat(orderItem.getTargetSnapshotId()).isEqualTo(entity.getTargetSnapshotId());
        assertThat(orderItem.getTargetType()).isEqualTo(entity.getTargetType());
        assertThat(orderItem.getSellerId()).isEqualTo(entity.getSellerId());
        assertThat(orderItem.getReceiverId()).isEqualTo(entity.getReceiverId());
        assertThat(orderItem.getPrice().amount()).isEqualByComparingTo(entity.getPrice());
        assertThat(orderItem.getQuantity().getValue()).isEqualTo(entity.getQuantity());
        assertThat(orderItem.getStatus()).isEqualTo(entity.getStatus());
    }
}
