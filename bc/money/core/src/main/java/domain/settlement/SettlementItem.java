package domain.settlement;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class SettlementItem extends BaseDomainModel {
    private Long orderId;                   // 결제/주문 식별자(현재는 결제)
    private String paymentKey;              // PG 결제 식별자
//    private String transactionKey;          // PG 트랜잭션 식별자(멱등키) (todo: 토스 대조 미정)

    private Long sellerId;                  // 판매자 식별자
    private SettlementType type;            // PAYMENT, CANCEL

    private Money totalAmount;              // 판매 금액
    private Money platformFee;              // 우리 수수료(₩)
    private Money pgFee;                    // pg 수수료(₩)
    private Money settlementAmount;         // 정산 금액(₩) (totalAmount - platformFee - pgFee)

    private SettlementStatus status;        // READY, WAIT, COMPLETE
    private LocalDateTime settlementDate;   // 정산 예정일

    private SettlementItem(Builder builder) {
        super(builder.id);
        this.orderId = builder.orderId;
        this.paymentKey = builder.paymentKey;
        this.sellerId = builder.sellerId;
        this.type = builder.type;
        this.totalAmount = builder.totalAmount;
        this.platformFee = builder.platformFee;
        this.pgFee = builder.pgFee;
        this.settlementAmount = builder.settlementAmount;
        this.status = builder.status;
        this.settlementDate = builder.settlementDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long orderId;
        private String paymentKey;
        private Long sellerId;
        private SettlementType type;
        private Money totalAmount;
        private Money platformFee;
        private Money pgFee;
        private Money settlementAmount;
        private SettlementStatus status;
        private LocalDateTime settlementDate;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder paymentKey(String paymentKey) {
            this.paymentKey = paymentKey;
            return this;
        }

        public Builder sellerId(Long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder type(SettlementType type) {
            this.type = type;
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder platformFee(Money platformFee) {
            this.platformFee = platformFee;
            return this;
        }

        public Builder pgFee(Money pgFee) {
            this.pgFee = pgFee;
            return this;
        }

        public Builder settlementAmount(Money settlementAmount) {
            this.settlementAmount = settlementAmount;
            return this;
        }

        public Builder status(SettlementStatus status) {
            this.status = status;
            return this;
        }

        public Builder settlementDate(LocalDateTime settlementDate) {
            this.settlementDate = settlementDate;
            return this;
        }

        public SettlementItem build() {
            return new SettlementItem(this);
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public SettlementType getType() {
        return type;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Money getPlatformFee() {
        return platformFee;
    }

    public Money getPgFee() {
        return pgFee;
    }

    public Money getSettlementAmount() {
        return settlementAmount;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }
}
