package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class PaymentRefundedEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final PaymentType type;
    private final String reason;
    private final LocalDateTime refundedAt;  // canceledAt 패턴과 일관

    public PaymentRefundedEvent(
        Long paymentId,
        String sourceType,
        Long userId,
        Money amount,
        PaymentType type,
        String reason,
        LocalDateTime refundedAt
    ) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.refundedAt = refundedAt;
    }

    // Getters
    public Long getPaymentId() { return paymentId; }
    public String getSourceType() { return sourceType; }
    public Long getUserId() { return userId; }
    public Money getAmount() { return amount; }
    public PaymentType getType() { return type; }
    public String getReason() { return reason; }
    public LocalDateTime getRefundedAt() { return refundedAt; }

    @Override
    public String toString() {
        return "PaymentRefundedEvent{" +
            "paymentId=" + paymentId +
            ", sourceType='" + sourceType + '\'' +
            ", userId=" + userId +
            ", amount=" + amount +
            ", type=" + type +
            ", reason='" + reason + '\'' +
            ", refundedAt=" + refundedAt +
            ", eventId='" + getEventId() + "'" +
            ", occurredAt=" + getOccurredAt() +
            '}';
    }
}
