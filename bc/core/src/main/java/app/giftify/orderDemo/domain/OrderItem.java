package app.giftify.orderDemo.domain;

import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long receiverId;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money unitPrice;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money amount;

    @Column(nullable = false)
    private OrderItemStatus status;

    @Column
    private LocalDateTime confirmedAt;

    @Column
    private LocalDateTime cancelledAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public OrderItem(Long orderId,
                     Long targetId,
                     TargetType targetType,
                     Long sellerId,
                     Long receiverId,
                     Money unitPrice,
                     Money amount,
                     OrderItemStatus status) {
        this.orderId = orderId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.sellerId = sellerId;
        this.receiverId = receiverId;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.status = status;
    }
}
