package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.time.LocalDateTime;

/**
 * 펀딩 생성 이벤트
 * - Member BC에서 수신하여 WishlistItem 상태 변경 (PENDING → IN_PROGRESS)
 */
public class FundingCreatedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final LocalDateTime deadline;

    public FundingCreatedEvent(Long fundingId, Long wishlistItemId, LocalDateTime deadline) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.deadline = deadline;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    @Override
    public String toString() {
        return "FundingCreatedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", deadline=" + deadline +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}


