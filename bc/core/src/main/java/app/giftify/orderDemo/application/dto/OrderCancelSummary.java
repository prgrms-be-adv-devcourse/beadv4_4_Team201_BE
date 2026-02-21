package app.giftify.orderDemo.application.dto;

import app.giftify.orderDemo.domain.OrderCancelResultCode;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record OrderCancelSummary(
        List<OrderItemCancelResult> items,
        OrderCancelResultCode orderCancelResultCode,
        Money totalCancelAmount
) {
    public static OrderCancelSummary of(List<OrderItemCancelResult> items) {
        return new OrderCancelSummary(
                items,
                determineOverallResult(items),
                calculateTotalCancelAmount(items)
        );
    }

    private static OrderCancelResultCode determineOverallResult(List<OrderItemCancelResult> items) {
        // todo
        return null;
    }

    private static Money calculateTotalCancelAmount(List<OrderItemCancelResult> items) {
        // todo
        return null;
    }
}
