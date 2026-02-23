package app.giftify.orderDemo.application.inbound.command;

import java.util.List;

public record CancelOrderItemsCommand(
        Long orderId,
        Long memberId,
        List<Long> itemIds
) {
}
