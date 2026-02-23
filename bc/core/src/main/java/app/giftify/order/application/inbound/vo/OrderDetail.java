package app.giftify.order.application.inbound.vo;

import java.util.List;

public record OrderDetail(
        OrderSummary order,
        List<OrderItemDetail> items
) {
}
