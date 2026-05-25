package app.giftify.wishlist.domain.port;

import app.giftify.wishlist.domain.vo.WishlistItemSnapshot;

import java.util.List;
import java.util.Map;

public interface WishlistItemSnapshotPort {
    WishlistItemSnapshot getSnapshot(Long wishlistItemId);

    Map<Long, WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemIds);
}
