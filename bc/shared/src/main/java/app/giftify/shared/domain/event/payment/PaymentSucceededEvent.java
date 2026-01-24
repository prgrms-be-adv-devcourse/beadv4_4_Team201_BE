package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public class PaymentSucceededEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final PaymentType type;
    private final LocalDateTime paidAt;

    public PaymentSucceededEvent(
        Long paymentId,
        String sourceType,
        Long userId,
        Money amount,
        PaymentType type,
        LocalDateTime paidAt
    ) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.paidAt = paidAt;
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

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    @Override
    public String toString() {
        return "PaymentSucceededEvent{" +
                "paymentId=" + paymentId +
                ", sourceType='" + sourceType + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                ", type=" + type +
                ", paidAt=" + paidAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
