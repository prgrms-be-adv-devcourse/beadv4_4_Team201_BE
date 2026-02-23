package app.giftify.order.adapter.inbound.web.dto.response;

import app.giftify.order.application.inbound.vo.OrderDetail;

public record GetOrderDetailResponse(
        OrderDetail orderDetail
) {
}
