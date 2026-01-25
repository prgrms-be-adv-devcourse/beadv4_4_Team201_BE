package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class PaymentRefundedForOrderEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String reason;

    public PaymentRefundedForOrderEvent(Long orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "PaymentRefundedForOrderEvent{" +
                "orderId=" + orderId +
                ", reason='" + reason + '\'' +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}