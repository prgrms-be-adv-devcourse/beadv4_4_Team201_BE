package app.giftify.wishlist.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

public class WishlistItemRemovedEvent extends BaseDomainEvent {
    private final Long wishlistItemId;
    private final Long productId;
    private final Long memberId;

    public WishlistItemRemovedEvent(Long wishlistItemId, Long productId, Long memberId) {
        super();
        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.memberId = memberId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public String toString() {
        return "WishlistItemRemovedEvent{" +
                "wishlistItemId=" + wishlistItemId +
                ", productId=" + productId +
                ", memberId=" + memberId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
