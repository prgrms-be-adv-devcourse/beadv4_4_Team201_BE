package app.giftify.settlement.application.dto;

import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.snapshot.OrderSnapshot;
import app.giftify.settlement.domain.snapshot.PaymentSnapshot;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public record SettlementSource(
        OrderItemSnapshot item,
        OrderSnapshot order,
        PaymentSnapshot payment
) {
    public Long getOrderItemId() {
        return item.getOrderItemId();
    }

    public Long getOrderId() {
        return order.getOrderId();
    }

    public Long getFunding() {
        return item.getTargetId();
    }

    public Long getSellerId() {
        return item.getSellerId();
    }

    public String getOrderNumber() {
        return order.getOrderNumber();
    }

    public LocalDateTime getOrderedAt() {
        return order.getOrderedAt();
    }

    public LocalDateTime getPaidAt() {
        return payment.getPaidAt();
    }

    public Money getPaidAmount() {
        return payment.getPaidAmount();
    }
}
