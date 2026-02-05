package app.giftify.wishlist.application.port.in;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

import java.util.List;

public interface GetWishlistItemSnapshotUseCase {
    WishlistItemSnapshot getSnapshot(Long wishlistItemId);

    List<WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemId);
}
