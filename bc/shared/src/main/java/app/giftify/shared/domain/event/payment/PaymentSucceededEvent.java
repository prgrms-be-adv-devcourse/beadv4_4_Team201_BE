package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentSucceededEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final PaymentType type;

    public PaymentSucceededEvent(Long paymentId, String sourceType, Long userId, Money amount, PaymentType type) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
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


    @Override
    public String toString() {
        return "PaymentSucceededEvent{" +
                "paymentId=" + paymentId +
                ", sourceType='" + sourceType + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                ", type=" + type +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
