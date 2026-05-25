package app.giftify.order.domain;

import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.api.exception.PolicyException;
import app.giftify.order.domain.event.OrderItemCreatedEvent;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id_status", columnList = "order_id, status")
})
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
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderItemType orderItemType;

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
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    @Column
    @Setter
    private String originTransactionKey;

    @Column
    private LocalDateTime cancelRequestedAt;

    @Column
    private LocalDateTime cancelledAt;

    @Column
    private LocalDateTime confirmedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static OrderItem create(
            Long targetId,
            TargetType targetType,
            OrderItemType orderItemType,
            Long sellerId,
            Long receiverId,
            Money price,
            Money amount
    ) {
        if (targetId == null) throw new DomainException(OrderErrorCode.INVALID_TARGET_ID);
        if (targetType == null) throw new DomainException(OrderErrorCode.INVALID_TARGET_TYPE);
        if (orderItemType == null) throw new DomainException(OrderErrorCode.INVALID_ORDER_TYPE);
        if (sellerId == null) throw new DomainException(OrderErrorCode.INVALID_SELLER_ID);
        if (receiverId == null) throw new DomainException(OrderErrorCode.INVALID_RECEIVER_ID);
        if (price == null || price.isLessThanOrEqual(Money.zero())) throw new DomainException(OrderErrorCode.INVALID_PRICE);
        if (amount == null) throw new DomainException(OrderErrorCode.INVALID_AMOUNT);

        if (amount.isGreaterThan(price)) throw new DomainException(OrderErrorCode.AMOUNT_EXCEEDS_PRICE);

        return OrderItem.builder()
                .targetId(targetId)
                .targetType(targetType)
                .orderItemType(orderItemType)
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
                .orderItemType(orderItemType)
                .sellerId(sellerId)
                .receiverId(receiverId)
                .price(price)
                .amount(amount)
                .status(status)
                .build();
    }

    public void updateTargetToFunding(Long fundingId) {
        this.targetId = fundingId;
        this.targetType = TargetType.FUNDING;

        if (order == null) throw new PolicyException(OrderErrorCode.ORDER_ITEM_NOT_ASSOCIATED);

        OrderItemCreatedEvent event = new OrderItemCreatedEvent(
                id,
                targetId,
                targetType,
                orderItemType,
                order.getId(),
                sellerId,
                price,
                amount
        );

        order.registerEvent(event);
    }

    @Override
    public String toString() {
        Long orderId = (order != null ? order.getId() : null);
        return "OrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", wishlistItemId=" + targetId +
                ", targetType=" + targetType +
                ", orderItemType=" + orderItemType +
                ", sellerId=" + sellerId +
                ", receiverId=" + receiverId +
                ", price=" + price +
                ", amount=" + amount +
                '}';
    }

    public void paid(String originTransactionKey) {
        if (status != OrderItemStatus.CREATED) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 아이템 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", status)
            );
        }

        status = OrderItemStatus.PAID;
        this.originTransactionKey = originTransactionKey;
    }

    public void canceling() {
        this.status = OrderItemStatus.CANCELING;
        this.cancelRequestedAt = LocalDateTime.now();
    }

    public void canceled() {
        this.status = OrderItemStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void failCancel() {
        status = OrderItemStatus.PAID;
    }

    public boolean isCancelable() {
        return status == OrderItemStatus.CREATED || status == OrderItemStatus.PAID;
    }

    public ResultCode decideCancelResult() {
        return switch (this.status) {
            case OrderItemStatus.CREATED -> ResultCode.SUCCESS;
            case OrderItemStatus.PAID -> ResultCode.ACCEPTED;
            case OrderItemStatus.CANCELING -> ResultCode.IN_PROGRESS;
            case OrderItemStatus.CANCELED -> ResultCode.ALREADY_PROCESSED;
            case OrderItemStatus.CONFIRMED -> ResultCode.FAIL;
            case OrderItemStatus.EXPIRED -> ResultCode.FAIL;
        };
    }

    public boolean isCanceling() {
        return status == OrderItemStatus.CANCELING;
    }

    public void confirmed() {
        if (status != OrderItemStatus.PAID) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 확정이 불가능한 상태입니다. orderItemId: %d, status: %s", id, status)
            );
        }

        status = OrderItemStatus.CONFIRMED;
        confirmedAt = LocalDateTime.now();
    }

    public void expired() {
        if (status != OrderItemStatus.CREATED) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 만료가 불가능한 상태입니다. orderItemId: %d, status: %s", id, status)
            );
        }
        status = OrderItemStatus.EXPIRED;
    }
}
