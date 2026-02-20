package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class PaymentCanceledEvent extends BaseDomainEvent {
    private final Long paymentId;
    private final String sourceType;
    private final Long userId;
    private final Money amount;
    private final String reason;
    private final PaymentType type;
    private final LocalDateTime canceledAt;
    private final Long orderId;

    public PaymentCanceledEvent(Long paymentId, String sourceType, Long userId, Money amount, String reason, PaymentType type, LocalDateTime canceledAt, Long orderId) {
        super();
        this.paymentId = paymentId;
        this.sourceType = sourceType;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.type = type;
        this.canceledAt = canceledAt;
        this.orderId = orderId;
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

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "PaymentCanceledEvent{" +
                "paymentId=" + paymentId +
                ", sourceType='" + sourceType + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                ", reason='" + reason + '\'' +
                ", type=" + type +
                ", canceledAt=" + canceledAt +
                ", orderId=" + orderId +
                '}';
    }
}
