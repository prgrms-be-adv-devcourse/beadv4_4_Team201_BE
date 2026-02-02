package app.giftify.funding.application.outbound;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

public interface WishlistItemSnapshotPort {
    WishlistItemSnapshot getSnapshot(Long wishlistItemId);
}
