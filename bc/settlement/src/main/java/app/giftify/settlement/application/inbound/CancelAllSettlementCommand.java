package app.giftify.settlement.application.inbound;

import java.util.List;

public record CancelAllSettlementCommand(
        Long orderId,
        List<Long> itemIds
) {
}
