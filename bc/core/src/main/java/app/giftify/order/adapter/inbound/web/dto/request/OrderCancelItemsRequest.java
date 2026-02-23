package app.giftify.order.adapter.inbound.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCancelItemsRequest(
        @NotNull @NotEmpty @Valid
        List<Long> itemIds
) {
}
