package app.giftify.shared.domain.event.funding;

import java.time.LocalDateTime;

public record FundingReceivedConfirmedEvent(
        Long fundingId,
        Long orderItemId,
        LocalDateTime confirmedAt
) {
}
