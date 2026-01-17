package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentRefundedEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String refundId;
    private final String sourceType;
    private final Long userId;
    private final Money refundAmount;
    private final String reason;
    private final PaymentType type;

    public PaymentRefundedEvent(Long paymentId, String refundId, String sourceType, Long userId, Money refundAmount, PaymentType type, String reason) {
        super();
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.type = type;
        this.reason = reason;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getRefundId() {
        return refundId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getRefundAmount() {
        return refundAmount;
    }

    public PaymentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "PaymentRefundedEvent{" +
                "paymentId=" + paymentId +
                ", refundId='" + refundId + "'" +
                ", sourceType='" + sourceType + "'" +
                ", userId=" + userId +
                ", refundAmount=" + refundAmount +
                ", type=" + type +
                ", reason='" + reason + "'" +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
