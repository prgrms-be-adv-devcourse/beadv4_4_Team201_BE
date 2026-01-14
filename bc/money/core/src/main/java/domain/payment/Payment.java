package domain.payment;

import vo.Money;

import java.time.LocalDateTime;

public class Payment {
    private Long paymentId;
    private final Long userId;
    private final PaymentType type;
    private PaymentStatus status;
    private final Money amount;
    private final Long fundingId;             // nullable (펀딩 결제일 경우에만 활성화)
    private String pgTransactionId;        // PG사 거래 ID
    private final PaymentMethod method;
    private final LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime settledAt;

    private Payment(
            Long paymentId, Long userId, PaymentType type, PaymentStatus status,
            Money amount, Long fundingId, String pgTransactionId, PaymentMethod method,
            LocalDateTime createdAt, LocalDateTime paidAt, LocalDateTime refundedAt, LocalDateTime settledAt
    ) {
        // 펀딩 결제일 경우 fundingId 필수
        if (type == PaymentType.FUNDING && fundingId == null) {
            throw new IllegalArgumentException("펀딩 결제는 fundingId가 필수입니다.");
        }

        this.paymentId = paymentId;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.fundingId = fundingId;
        this.pgTransactionId = pgTransactionId;
        this.method = method;
        this.createdAt = createdAt;
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
        private LocalDateTime createdAt;
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

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
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
            LocalDateTime finalCreatedAt = createdAt != null ? createdAt : LocalDateTime.now();
            return new Payment(
                    paymentId,
                    userId,
                    type,
                    status,
                    amount,
                    fundingId,
                    pgTransactionId,
                    method,
                    finalCreatedAt, // 기본값 처리
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
        return new Payment(
                id, this.userId, this.type, this.status,
                this.amount, this.fundingId, this.pgTransactionId, this.method,
                this.createdAt, this.paidAt, this.refundedAt, this.settledAt
        );
    }

    public void settle() {
        if (this.status != PaymentStatus.PAID) {
            throw new IllegalStateException("결제 완료(PAID) 상태에서만 확정할 수 있습니다.");
        }
        this.status = PaymentStatus.SETTLED;
        this.settledAt = LocalDateTime.now();
    }

    public void refund() {
        if (this.status == PaymentStatus.SETTLED) {
            throw new IllegalStateException("이미 수령 처리되어 환불할 수 없습니다.");
        }
        if (!this.status.canRefund()) {
            throw new IllegalStateException("환불 불가능한 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("취소 불가능한 상태입니다: " + this.status);
        }
        this.status = PaymentStatus.CANCELED;
    }

    public void markAsPaid(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("결제 대기(PENDING) 상태에서만 완료 처리할 수 있습니다.");
        }
        this.status = PaymentStatus.PAID;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = LocalDateTime.now();
    }

    public boolean isRefundable() {
        return this.status == PaymentStatus.PAID && this.refundedAt == null && this.settledAt == null;
    }

    public boolean isCancelable() {
        return this.status == PaymentStatus.PENDING;
    }

    public Long getPaymentId() {
        return paymentId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
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
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", type=" + type +
                ", status=" + status +
                ", amount=" + amount +
                ", fundingId=" + fundingId +
                ", pgTransactionId='" + pgTransactionId + '\'' +
                ", method=" + method +
                ", createdAt=" + createdAt +
                ", paidAt=" + paidAt +
                ", refundedAt=" + refundedAt +
                ", settledAt=" + settledAt +
                '}';
    }
}
