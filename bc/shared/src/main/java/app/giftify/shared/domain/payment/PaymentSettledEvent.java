package app.giftify.shared.domain.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentSettledEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final LocalDateTime settledAt;
    private final PaymentType type;

    public PaymentSettledEvent(Long paymentId, String sourceType, Long userId, Money amount, PaymentType type, LocalDateTime settledAt) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.settledAt = settledAt;
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

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    @Override
    public String toString() {
        return "PaymentSettledEvent{" +
                "paymentId=" + paymentId + 
                ", sourceType='" + sourceType + "'" + 
                ", userId=" + userId + 
                ", amount=" + amount + 
                ", type=" + type + 
                ", settledAt=" + settledAt + 
                ", eventId='" + getEventId() + "'" + 
                ", occurredAt=" + getOccurredAt() + 
                '}';
    }
}