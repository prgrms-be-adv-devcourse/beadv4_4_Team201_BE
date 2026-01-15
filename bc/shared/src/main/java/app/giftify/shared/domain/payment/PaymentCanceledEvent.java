package app.giftify.shared.domain.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentCanceledEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final Long userId;
    private final Money amount;
    private final String reason;

    public PaymentCanceledEvent(Long paymentId, Long userId, Money amount, String reason) {
        super();
        this.paymentId = paymentId;
        this.userId = userId;
        this.amount = amount;
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

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "PaymentCanceledEvent{" +
                "paymentId=" + paymentId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", reason='" + reason + "'" + 
                ", eventId='" + getEventId() + "'" + 
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}