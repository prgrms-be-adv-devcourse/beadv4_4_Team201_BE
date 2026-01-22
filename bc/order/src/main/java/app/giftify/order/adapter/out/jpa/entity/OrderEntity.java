package app.giftify.order.adapter.out.jpa.entity;

import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 주문 정보를 담는 JPA 엔티티
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderEntity extends BaseJpaEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "confirmed_at")
    private java.time.LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // 엔티티 계층에서 관리하는 수정 일시 반환
    public LocalDateTime updatedAt() {
        return super.getUpdatedAt();
    }
}
