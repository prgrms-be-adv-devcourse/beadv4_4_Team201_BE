package app.giftify.orderDemo.application.dto;

import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record OrderCancelSummary(
        List<OrderItemCancelResult> items,
        OrderCancelResult overallResult,
        Money totalCancelAmount
) {
    public static OrderCancelSummary of(List<OrderItemCancelResult> items) {
        return new OrderCancelSummary(
                items,
                determineOverallResult(items),
                calculateTotalCancelAmount(items)
        );
    }

    private static OrderCancelResult determineOverallResult(List<OrderItemCancelResult> items) {
        // todo
        return null;
    }

    private static Money calculateTotalCancelAmount(List<OrderItemCancelResult> items) {
        // todo
        return null;
    }
}
