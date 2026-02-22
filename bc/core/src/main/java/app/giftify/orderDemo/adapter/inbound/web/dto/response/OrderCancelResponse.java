package app.giftify.orderDemo.adapter.inbound.web.dto.response;

import app.giftify.orderDemo.application.dto.OrderItemCancelResult;
import app.giftify.orderDemo.application.dto.OrderCancelSummary;
import app.giftify.shared.domain.vo.Money;

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
