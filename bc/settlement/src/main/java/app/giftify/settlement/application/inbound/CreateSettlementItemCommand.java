package app.giftify.settlement.application.inbound;

import java.time.LocalDateTime;

public record CreateSettlementItemCommand(
        Long fundingId,
        LocalDateTime confirmedAt
) {
}
