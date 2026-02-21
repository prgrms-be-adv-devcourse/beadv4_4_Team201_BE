package app.giftify.orderDemo.application.dto;

import app.giftify.orderDemo.domain.CancelTargetItems;

import java.util.List;

public record OrderCancelProcessingResult(
        List<OrderItemCancelResult> results,
        CancelTargetItems pendingItems
) {
}
