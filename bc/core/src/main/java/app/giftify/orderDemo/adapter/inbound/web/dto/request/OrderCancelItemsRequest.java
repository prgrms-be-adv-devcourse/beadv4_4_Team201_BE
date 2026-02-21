package app.giftify.orderDemo.adapter.inbound.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCancelItemsRequest(
        @NotNull @NotEmpty @Valid
        List<OrderCancelItemRequest> items
) {
    public List<Long> getIds() {
        return items.stream()
                .map(OrderCancelItemRequest::itemId)
                .toList();
    }
}
