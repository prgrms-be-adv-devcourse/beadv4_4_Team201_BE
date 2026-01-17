package domain.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class Payment extends BaseDomainModel {
    private final Long userId;
    private final PaymentType type;
    private PaymentStatus status;
    private final Money amount;
    private final Long fundingId;             // nullable (펀딩 결제일 경우에만 활성화)
    private String pgTransactionId;           // PG사 거래 ID
    private final PaymentMethod method;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime settledAt;

    private Payment(
            Long id, Long userId, PaymentType type, PaymentStatus status,
            Money amount, Long fundingId, String pgTransactionId, PaymentMethod method,
            LocalDateTime paidAt, LocalDateTime refundedAt, LocalDateTime settledAt
    ) {
        super(id);
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.fundingId = fundingId;
        this.pgTransactionId = pgTransactionId;
        this.method = method;
        this.paidAt = paidAt;
        this.refundedAt = refundedAt;
        this.settledAt = settledAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long paymentId;
        private Long userId;
        private PaymentType type;
        private PaymentStatus status;
        private Money amount;
        private Long fundingId;
        private String pgTransactionId;
        private PaymentMethod method;
        private LocalDateTime paidAt;
        private LocalDateTime refundedAt;
        private LocalDateTime settledAt;

        public Builder paymentId(Long paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder type(PaymentType type) {
            this.type = type;
            return this;
        }

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder fundingId(Long fundingId) {
            this.fundingId = fundingId;
            return this;
        }

        public Builder pgTransactionId(String pgTransactionId) {
            this.pgTransactionId = pgTransactionId;
            return this;
        }

        public Builder method(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder paidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder refundedAt(LocalDateTime refundedAt) {
            this.refundedAt = refundedAt;
            return this;
        }

        public Builder settledAt(LocalDateTime settledAt) {
            this.settledAt = settledAt;
            return this;
        }

        public Payment build() {
            return new Payment(
                    paymentId,
                    userId,
                    type,
                    status,
                    amount,
                    fundingId,
                    pgTransactionId,
                    method,
                    paidAt,
                    refundedAt,
                    settledAt
            );
        }
    }

    public static Payment createPaidForFunding(Long userId, Long fundingId, Money amount) {
        return Payment.builder()
            .userId(userId)
            .type(PaymentType.FUNDING)
            .status(PaymentStatus.PAID)
            .amount(amount)
            .fundingId(fundingId)
            .method(PaymentMethod.GIFTIFY_CASH)
            .paidAt(LocalDateTime.now())
            .build();
    }

    public Payment withId(Long id) {
        return Payment.builder()
            .paymentId(id)
            .userId(this.userId)
            .type(this.type)
            .status(this.status)
            .amount(this.amount)
            .fundingId(this.fundingId)
            .pgTransactionId(this.pgTransactionId)
            .method(this.method)
            .paidAt(this.paidAt)
            .refundedAt(this.refundedAt)
            .settledAt(this.settledAt)
            .build();
    }

    public void settle() {
        if (this.status != PaymentStatus.PAID) {
            throw new IllegalStateException("[Payment] 결제 완료(PAID) 상태에서만 확정할 수 있습니다.");
        }
        this.status = PaymentStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }

    public void refund() {
        if (this.status == PaymentStatus.SETTLED) {
            throw new IllegalStateException("[Payment] 이미 수령 처리되어 환불할 수 없습니다.");
        }
        if (!this.status.canRefund()) {
            throw new IllegalStateException("[Payment] 환불 불가능한 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("[Payment] 취소 불가능한 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.CANCELED;
    }

    public void markAsPaid(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("[Payment] 결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.PAID;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("[Payment] 대기 중인 결제만 실패 처리할 수 있습니다. 현재 상태: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
    }


    public boolean isRefundable() {
        return this.status == PaymentStatus.PAID && this.refundedAt == null && this.settledAt == null;
    }

    public boolean isCancelable() {
        return this.status == PaymentStatus.PENDING;
    }

    public Long getPaymentId() {
        return getId();
    }

    public Long getUserId() {
        return userId;
    }

    public PaymentType getType() {
        return type;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Money getAmount() {
        return amount;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + getId() +
                ", modelType=" + getModelType() +
                ", userId=" + userId +
                ", type=" + type +
                ", status=" + status +
                ", amount=" + amount +
                ", fundingId=" + fundingId +
                ", pgTransactionId='" + pgTransactionId + "'" +
                ", method=" + method +
                ", paidAt=" + paidAt +
                ", refundedAt=" + refundedAt +
                ", settledAt=" + settledAt +
                '}';
    }
}
