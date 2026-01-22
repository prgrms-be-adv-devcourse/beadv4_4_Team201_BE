package app.giftify.order.adapter.out.jpa.entity;

import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 주문 아이템 정보를 담는 JPA 엔티티
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderItemEntity extends BaseJpaEntity {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "funding_id", nullable = false)
    private Long fundingId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "confirmed_at")
    private java.time.LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;

    // 엔티티 계층에서 관리하는 수정 일시 반환
    public LocalDateTime updatedAt() {
        return super.getUpdatedAt();
    }
}
