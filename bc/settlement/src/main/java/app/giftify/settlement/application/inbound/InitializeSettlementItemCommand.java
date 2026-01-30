package app.giftify.settlement.application.inbound;

import java.time.LocalDateTime;

public record InitializeSettlementItemCommand(
        Long fundingId,
        LocalDateTime confirmedAt
) {
}
