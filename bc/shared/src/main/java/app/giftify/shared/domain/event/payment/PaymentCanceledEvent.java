package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentCanceledEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final String reason;
    private final PaymentType type;

    public PaymentCanceledEvent(Long paymentId, String sourceType, Long userId, Money amount, PaymentType type, String reason) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getSourceType() {
        return sourceType;
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
        return "PaymentCanceledEvent{" +
                "paymentId=" + paymentId + 
                ", sourceType='" + sourceType + "'" + 
                ", userId=" + userId + 
                ", amount=" + amount + 
                ", type=" + type + 
                ", reason='" + reason + "'" + 
                ", eventId='" + getEventId() + "'" + 
                ", occurredAt=" + getOccurredAt() + 
                '}';
    }
}
