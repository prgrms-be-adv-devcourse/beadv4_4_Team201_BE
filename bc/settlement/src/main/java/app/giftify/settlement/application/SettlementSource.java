package app.giftify.settlement.application;

import app.giftify.settlement.domain.*;
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
        return item.getFundingId();
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
