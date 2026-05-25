package app.giftify.order.adapter.inbound.web.dto.response;

import app.giftify.order.application.dto.OrderItemCancelResult;
import app.giftify.order.application.dto.OrderCancelSummary;
import app.giftify.support.common.money.Money;

import java.util.List;

public record OrderCancelResponse(
        Long orderId,
        Money totalCancelAmount,
        List<OrderItemCancelResult> items
) {
    public static OrderCancelResponse of(Long orderId, OrderCancelSummary result) {
        return new OrderCancelResponse(
                orderId,
                result.cancelAmount(),
                result.items()
        );
    }
}
