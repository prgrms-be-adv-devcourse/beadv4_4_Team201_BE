package app.giftify.orderDemo.adapter.inbound.web.dto.response;

import app.giftify.orderDemo.application.inbound.vo.OrderSummary;

import java.util.List;

public record GetOrdersResponse(
        List<OrderSummary> orders,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
