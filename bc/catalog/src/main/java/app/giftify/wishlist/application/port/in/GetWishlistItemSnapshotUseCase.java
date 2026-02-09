package app.giftify.wishlist.application.port.in;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

import java.util.List;
import java.util.Map;

public interface GetWishlistItemSnapshotUseCase {
    Map<Long, WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemId);
}
