package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.exception.DomainException;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

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
    private Money price;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false, precision = 19, scale = 2)
    private Money amount;

    @Column(nullable = false)
    private OrderItemStatus status;

    @Column
    private LocalDateTime cancelledAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static OrderItem create(
            Long targetId,
            TargetType targetType,
            Long sellerId,
            Long receiverId,
            Money price,
            Money amount
    ) {
        if (targetId == null) throw new DomainException(OrderErrorCode.INVALID_TARGET_ID);
        if (targetType == null) throw new DomainException(OrderErrorCode.INVALID_TARGET_TYPE);
        if (sellerId == null) throw new DomainException(OrderErrorCode.INVALID_SELLER_ID);
        if (receiverId == null) throw new DomainException(OrderErrorCode.INVALID_RECEIVER_ID);
        if (price == null || price.isLessThanOrEqual(Money.zero())) throw new DomainException(OrderErrorCode.INVALID_PRICE);
        if (amount == null) throw new DomainException(OrderErrorCode.INVALID_AMOUNT);

        if (amount.isGreaterThan(price)) throw new DomainException(OrderErrorCode.AMOUNT_EXCEEDS_PRICE);

        return OrderItem.builder()
                .targetId(targetId)
                .targetType(targetType)
                .sellerId(sellerId)
                .receiverId(receiverId)
                .price(price)
                .amount(amount)
                .status(OrderItemStatus.CREATED)
                .build();
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public OrderItemSnapshot toSnapshot() {
        return OrderItemSnapshot.builder()
                .orderItemId(id)
                .targetId(targetId)
                .targetType(targetType)
                .sellerId(sellerId)
                .receiverId(receiverId)
                .price(price)
                .amount(amount)
                .status(status)
                .build();
    }
}
