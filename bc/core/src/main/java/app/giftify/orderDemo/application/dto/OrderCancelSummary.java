package app.giftify.orderDemo.application.dto;

import app.giftify.orderDemo.domain.ResultCode;
import app.giftify.shared.domain.vo.Money;

import java.util.List;

public record OrderCancelSummary(
        List<OrderItemCancelResult> items,
        ResultCode overallResultCode,
        Money cancelAmount
) {
    public static OrderCancelSummary of(List<OrderItemCancelResult> items, Money cancelAmount) {
        return new OrderCancelSummary(
                items,
                determineOverallResult(items),
                cancelAmount
        );
    }

    private static ResultCode determineOverallResult(List<OrderItemCancelResult> items) {
        // todo
        return null;
    }
}
