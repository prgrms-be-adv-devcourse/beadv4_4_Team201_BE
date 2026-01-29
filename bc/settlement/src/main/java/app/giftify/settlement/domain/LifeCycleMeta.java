package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public record LifeCycleMeta(
        SettlementItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime settledAt,
        LocalDateTime cancelledAt
) {
}
