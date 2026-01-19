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
    private final Integer targetAmount;
    private final LocalDateTime endAt;

    public FundingCreatedEvent(Long fundingId, Long wishlistItemId, Integer targetAmount, LocalDateTime endAt) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.targetAmount = targetAmount;
        this.endAt = endAt;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Integer getTargetAmount() {
        return targetAmount;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    @Override
    public String toString() {
        return "FundingCreatedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", targetAmount=" + targetAmount +
                ", endAt=" + endAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

