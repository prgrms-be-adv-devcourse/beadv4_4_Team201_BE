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
    private LocalDateTime canceledAt;
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
        this.canceledAt = canceledAt;
    }

    public static OrderItem create(
            Long orderId,
            Long fundingId,
            Long productId,
            Long sellerId,
            Long receiverId,
            Money price,
            Quantity quantity
    ) {
        return new OrderItem(
                null,
                orderId,
                fundingId,
                productId,
                sellerId,
                receiverId,
                price,
                quantity,
                OrderStatus.PAYMENT_PENDING,
                LocalDateTime.now(),
                null,
                null
        );
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