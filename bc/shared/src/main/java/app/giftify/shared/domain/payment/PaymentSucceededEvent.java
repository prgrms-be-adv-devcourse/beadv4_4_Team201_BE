package app.giftify.shared.domain.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentSucceededEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String pgTransactionId;
    private final Long userId;
    private final Money amount;
    private final PaymentType type;
    private final Long fundingId;

    public PaymentSucceededEvent(Long paymentId, String pgTransactionId, Long userId, Money amount, PaymentType type, Long fundingId) {
        super();
        this.paymentId = paymentId;
        this.pgTransactionId = pgTransactionId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.fundingId = fundingId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getPgTransactionId() {
        return pgTransactionId;
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

    public Long getFundingId() {
        return fundingId;
    }

    @Override
    public String toString() {
        return "PaymentSucceededEvent{" +
                "paymentId=" + paymentId +
                ", pgTransactionId='" + pgTransactionId + "'" +
                ", userId=" + userId +
                ", amount=" + amount +
                ", type=" + type +
                ", fundingId=" + fundingId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
