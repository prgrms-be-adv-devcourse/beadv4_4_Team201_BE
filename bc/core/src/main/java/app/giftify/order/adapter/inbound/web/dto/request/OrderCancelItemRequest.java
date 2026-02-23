package app.giftify.order.adapter.inbound.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrderCancelItemRequest(
        @NotNull
        Long itemId
) {
}
