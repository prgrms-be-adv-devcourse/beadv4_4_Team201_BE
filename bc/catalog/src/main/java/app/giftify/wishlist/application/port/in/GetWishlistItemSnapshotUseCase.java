package app.giftify.wishlist.application.port.in;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

public interface GetWishlistItemSnapshotUseCase {
    WishlistItemSnapshot getSnapshot(Long wishlistItemId);
}
