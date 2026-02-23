package app.giftify.order.application.dto;

import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record OrderCancelSummary(
        List<OrderItemCancelResult> items,
        Money cancelAmount
) {
    public static OrderCancelSummary of(List<OrderItemCancelResult> items, Money cancelAmount) {
        return new OrderCancelSummary(
                items,
                cancelAmount
        );
    }
}
