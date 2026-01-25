package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class PaymentSucceededForOrderEvent extends BaseDomainEvent {
    private final Long orderId;
    private final String paymentKey;

    public PaymentSucceededForOrderEvent(Long orderId, String paymentKey) {
        super();
        this.orderId = orderId;
        this.paymentKey = paymentKey;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    @Override
    public String toString() {
        return "PaymentSucceededForOrderEvent{" +
                "orderId=" + orderId +
                ", paymentKey='" + paymentKey + '\'' +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}