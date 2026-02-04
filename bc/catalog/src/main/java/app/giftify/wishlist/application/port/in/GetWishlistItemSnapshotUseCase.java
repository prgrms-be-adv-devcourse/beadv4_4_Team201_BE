package app.giftify.wishlist.application.port.in;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

import java.util.List;

public interface GetWishlistItemSnapshotUseCase {
    List<WishlistItemSnapshot> getSnapshot(List<Long> wishlistItemId);
}
