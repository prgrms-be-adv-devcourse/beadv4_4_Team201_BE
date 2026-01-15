package app.giftify.shared.domain.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentFailedEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final Long userId;
    private final Money amount;
    private final PaymentType type;
    private final String reason;

    public PaymentFailedEvent(Long paymentId, Long userId, Money amount, PaymentType type, String reason) {
        super();
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getUserId() {
        return userId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "PaymentFailedEvent{" +
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", type=" + type +
                ", reason='" + reason + "'" +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}