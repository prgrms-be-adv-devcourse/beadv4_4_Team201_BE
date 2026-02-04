package app.giftify.orderDemo.application.inbound.vo;

import java.util.List;

public record OrderDetail(
        OrderSummary order,
        List<OrderItemDetail> items
) {
}
