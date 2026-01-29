package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public class FundingSnapshot {
    private Long fundingId;
    private Long orderItemId;
    private LocalDateTime confirmedAt;
}
