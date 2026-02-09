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
    private final Long orderItemId;

    public FundingCreatedEvent(Long fundingId, Long wishlistItemId, Long orderItemId) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.orderItemId = orderItemId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    @Override
    public String toString() {
        return "FundingCreatedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", orderItemId=" + orderItemId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
