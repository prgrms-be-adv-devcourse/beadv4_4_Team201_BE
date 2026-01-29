package app.giftify.settlement.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public record LifeCycleMeta(
        @Enumerated(EnumType.STRING)
        SettlementItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime scheduledAt,
        LocalDateTime settledAt,
        LocalDateTime cancelledAt
) {
}
