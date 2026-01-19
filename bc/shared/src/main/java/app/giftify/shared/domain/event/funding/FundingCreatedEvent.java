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
    private final LocalDateTime deadline;

    public FundingCreatedEvent(Long fundingId, Long wishlistItemId, Integer targetAmount, LocalDateTime deadline) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.targetAmount = targetAmount;
        this.deadline = deadline;
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

    public LocalDateTime getDeadline() {
        return deadline;
    }

    @Override
    public String toString() {
        return "FundingCreatedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", targetAmount=" + targetAmount +
                ", deadline=" + deadline +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}


