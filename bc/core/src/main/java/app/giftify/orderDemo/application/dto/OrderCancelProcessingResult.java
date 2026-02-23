package app.giftify.orderDemo.application.dto;

import app.giftify.orderDemo.domain.CancelTargetItems;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record OrderCancelProcessingResult(
        List<OrderItemCancelResult> results,
        CancelTargetItems pendingItems
) {
    public boolean hasPendingItems() {
        return !pendingItems.items().isEmpty();
    }

    public Money calculateCancelAmount() {
        return pendingItems.calculateCancelAmount();
    }
}
