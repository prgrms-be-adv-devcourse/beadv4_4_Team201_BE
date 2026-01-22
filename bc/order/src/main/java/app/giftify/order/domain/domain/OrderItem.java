package app.giftify.order.domain.domain;

import app.giftify.shared.domain.vo.Quantity;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class OrderItem extends BaseDomainModel {

    private final Long orderId;
    private final Long fundingId;
    private final Long productId;
    private final Long sellerId;
    private final Long receiverId;

    private final Money price;
    private final Quantity quantity; // Quantity VO로 랩핑

    private OrderStatus status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;

    protected OrderItem(
            Long id,
            Long orderId,
            Long fundingId,
            Long productId,
            Long sellerId,
            Long receiverId,
            Money price,
            Quantity quantity,
            OrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime confirmedAt,
            LocalDateTime canceledAt
    ) {
        super(id);
        this.orderId = orderId;
        this.fundingId = fundingId;
        this.productId = productId;
        this.sellerId = sellerId;
        this.receiverId = receiverId;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.cancelledAt = canceledAt;
    }

    // 결제 완료 처리
    // PAYMENT_PENDING 상태에서만 ORDERED로 변경 가능
    public void toOrdered() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("주문 대기 상태에서만 결제 완료로 변경 가능합니다.");
        }
        this.status = OrderStatus.ORDERED;
    }

    // 구매 확정 처리
    // ORDERED 상태에서만 CONFIRMED로 변경 가능
    public void toConfirmed() {
        if (this.status != OrderStatus.ORDERED) {
            throw new IllegalStateException("주문 결제 완료 상태에서만 확정이 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    // 주문 아이템 취소 처리
    // CONFIRMED 상태 이후에는 취소 불가
    public void toCancelled() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 주문 아이템은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELED;
        this.cancelledAt = LocalDateTime.now();
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getProductId() {
        return productId;
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
        private Long fundingId;
        private Long productId;
        private Long sellerId;
        private Long receiverId;
        private Money price;
        private Quantity quantity;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime confirmedAt;
        private LocalDateTime canceledAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder fundingId(Long fundingId) {
            this.fundingId = fundingId;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
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

        public Builder canceledAt(LocalDateTime canceledAt) {
            this.canceledAt = canceledAt;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(
                    id,
                    orderId,
                    fundingId,
                    productId,
                    sellerId,
                    receiverId,
                    price,
                    quantity,
                    status,
                    createdAt,
                    confirmedAt,
                    canceledAt
            );
        }
    }
}