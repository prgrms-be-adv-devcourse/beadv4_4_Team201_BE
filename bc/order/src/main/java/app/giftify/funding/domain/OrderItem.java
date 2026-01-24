package app.giftify.funding.domain;

import app.giftify.funding.domain.exception.OrderErrorCode;
import app.giftify.funding.domain.exception.OrderException;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;

import java.time.LocalDateTime;

public class OrderItem extends BaseDomainModel {

    private final Long orderId; // 주문 식별자

    private final Long targetSnapshotId; // 주문한 아이템의 스냅샷 ID
    private final TargetType targetType; // 주문한 아이템의 유형 (일반|펀딩|예치금|쿠폰)

    private final Long sellerId; // 판매자 식별자
    private final Long receiverId; // 구매 확정 주체 식별자

    private Quantity quantity; // 수량
    private Money price; // 단가 기준 금액

    private OrderStatus status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;

    protected OrderItem(
            Long id,
            Long orderId,
            Long targetSnapshotId,
            TargetType targetType,
            Long sellerId,
            Long receiverId,
            Money price,
            Quantity quantity,
            OrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime confirmedAt,
            LocalDateTime cancelledAt
    ) {
        super(id);
        this.orderId = orderId;
        this.targetSnapshotId = targetSnapshotId;
        this.targetType = targetType;
        this.sellerId = sellerId;
        this.receiverId = receiverId;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = cancelledAt;
    }

    // 결제 완료 처리
    // PAYMENT_PENDING → ORDERED
    public void toOrdered() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new OrderException(OrderErrorCode.ORDER_NOT_PAYABLE, "주문 대기 상태에서만 결제 완료로 변경 가능합니다.");
        }
        this.status = OrderStatus.ORDERED;
    }

    // 구매 확정 처리
    // ORDERED → CONFIRMED
    public void toConfirmed() {
        if (this.status != OrderStatus.ORDERED) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_CANNOT_BE_CANCELED, "주문 결제 완료 상태에서만 확정이 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // 주문 아이템 취소 처리
    // CONFIRMED 이후 취소 불가
    public void toCancelled() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new OrderException(OrderErrorCode.ORDER_ITEM_ALREADY_CONFIRMED, "이미 확정된 주문 아이템은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getTargetSnapshotId() {
        return targetSnapshotId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public Money getPrice() {
        return price;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long orderId;
        private Long targetSnapshotId;
        private TargetType targetType;
        private Long sellerId;
        private Long receiverId;
        private Money price;
        private Quantity quantity;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime cancelledAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder targetSnapshotId(Long targetSnapshotId) {
            this.targetSnapshotId = targetSnapshotId;
            return this;
        }

        public Builder targetType(TargetType targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder sellerId(Long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder receiverId(Long receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public Builder price(Money price) {
            this.price = price;
            return this;
        }

        public Builder quantity(Quantity quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder confirmedAt(LocalDateTime confirmedAt) {
            this.confirmedAt = confirmedAt;
            return this;
        }

        public Builder cancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(
                    id,
                    orderId,
                    targetSnapshotId,
                    targetType,
                    sellerId,
                    receiverId,
                    price,
                    quantity,
                    status,
                    createdAt,
                    confirmedAt,
                    cancelledAt
            );
        }
    }
}