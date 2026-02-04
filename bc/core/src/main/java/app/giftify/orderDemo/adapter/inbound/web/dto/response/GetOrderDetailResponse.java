package app.giftify.orderDemo.adapter.inbound.web.dto.response;

import app.giftify.orderDemo.application.inbound.vo.OrderDetail;

public record GetOrderDetailResponse(
        OrderDetail orderDetail
) {
}
