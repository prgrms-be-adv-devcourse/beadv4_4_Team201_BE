package app.giftify.shared.domain.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentSettledEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final Long userId;
    private final Money amount;
    private final LocalDateTime settledAt;

    public PaymentSettledEvent(Long paymentId, Long userId, Money amount, LocalDateTime settledAt) {
        super();
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
        this.settledAt = settledAt;
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

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    @Override
    public String toString() {
        return "PaymentSettledEvent{" +
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", settledAt=" + settledAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
