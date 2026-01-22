package app.giftify.order.domain.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class Order extends BaseDomainModel {

    private final String orderNumber;
    private final Long buyerId;
    private final LocalDateTime createdAt;

    private Money totalAmount;
    private OrderStatus status;
    private String paymentKey;

    private LocalDateTime confirmedAt;
    private LocalDateTime canceledAt;

    protected Order(
            Long id,
            String orderNumber,
            Long buyerId,
            Money totalAmount,
            OrderStatus status,
            LocalDateTime createdAt,
            String paymentKey,
            LocalDateTime confirmedAt,
            LocalDateTime canceledAt
    ) {
        super(id);
        this.orderNumber = orderNumber;
        this.buyerId = buyerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.paymentKey = paymentKey;
        this.confirmedAt = confirmedAt;
        this.canceledAt = canceledAt;
    }

    public static Order create(String orderNumber, Long buyerId, Money totalAmount) {
        return new Order(
                null,
                orderNumber,
                buyerId,
                totalAmount,
                OrderStatus.PAYMENT_PENDING,
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String orderNumber;
        private Long buyerId;
        private Money totalAmount;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private String paymentKey;
        private LocalDateTime confirmedAt;
        private LocalDateTime canceledAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder buyerId(Long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
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

        public Builder paymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
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

        public Order build() {
            return new Order(
                    id,
                    orderNumber,
                    buyerId,
                    totalAmount,
                    status,
                    createdAt,
                    paymentKey,
                    confirmedAt,
                    canceledAt
            );
        }
    }
}