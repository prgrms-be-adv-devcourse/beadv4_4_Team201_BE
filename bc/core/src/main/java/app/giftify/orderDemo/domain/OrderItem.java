package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item_v2", indexes = {
        @Index(name = "idx_order_items_order_id_status", columnList = "order_id, status")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Slf4j
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

    public void toPaid(String originTransactionKey) {
        if (this.status == OrderItemStatus.PAID) {
            return;
        }

        if (status != OrderItemStatus.CREATED) {
            throw new PolicyException(
                    OrderErrorCode.INVALID_STATUS_TRANSITION,
                    String.format("주문 아이템 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", status)
            );
        }
        status = OrderItemStatus.PAID;
        this.originTransactionKey = originTransactionKey;
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (status == OrderItemStatus.CANCELED) {
            log.warn("이미 취소된 주문입니다. orderId = {}, orderItemId = {}", order.getId(), id);
        }

        status = OrderItemStatus.CANCELED;
        this.cancelledAt = cancelledAt;
    }

    public void pendingToCancel(LocalDateTime cancelRequestedAt) {
        status = OrderItemStatus.CANCEL_PENDING;
        this.cancelRequestedAt = cancelRequestedAt;
    }

    public void failCancel() {
        status = OrderItemStatus.PAID;
    }
}
