package app.giftify.funding.application.outbound;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import java.util.List;

public interface WishlistItemSnapshotPort {
    List<WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemIds);
}
