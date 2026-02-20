package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import java.util.List;

public record OrderCancelItemsRequest(
        List<OrderCancelItemRequest> items
) {
    public List<Long> getIds() {
        return items.stream()
                .map(OrderCancelItemRequest::itemId)
                .toList();
    }
}
