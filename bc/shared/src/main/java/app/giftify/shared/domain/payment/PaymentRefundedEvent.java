package app.giftify.shared.domain.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentRefundedEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String refundId;
    private final Long userId;
    private final Money refundAmount;
    private final String reason;

    public PaymentRefundedEvent(Long paymentId, String refundId, Long userId, Money refundAmount, String reason) {
        super();
        this.paymentId = paymentId;
        this.refundId = refundId;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.reason = reason;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getRefundId() {
        return refundId;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getRefundAmount() {
        return refundAmount;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "PaymentRefundedEvent{" +
                "paymentId=" + paymentId +
                ", refundId='" + refundId + "'" +
                ", userId=" + userId +
                ", refundAmount=" + refundAmount +
                ", reason='" + reason + "'" +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}