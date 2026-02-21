package app.giftify.orderDemo.application.inbound.command;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.OrderCancelItemsRequest;

import java.util.List;

public record CancelOrderItemsCommand(
        Long orderId,
        Long memberId,
        List<Long> itemIds
) {
    public static CancelOrderItemsCommand of(Long orderId, Long memberId, OrderCancelItemsRequest request) {
        return new CancelOrderItemsCommand(
                orderId,
                memberId,
                request.itemIds()
        );
    }
}
