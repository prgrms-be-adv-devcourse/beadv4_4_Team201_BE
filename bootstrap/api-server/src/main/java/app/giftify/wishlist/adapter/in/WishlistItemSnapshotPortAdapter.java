package app.giftify.wishlist.adapter.in;

import app.giftify.wishlist.domain.port.WishlistItemSnapshotPort;
import app.giftify.wishlist.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WishlistItemSnapshotPortAdapter implements WishlistItemSnapshotPort {

    private final GetWishlistItemSnapshotUseCase getWishlistItemSnapshotUseCase;

    @Override
    public WishlistItemSnapshot getSnapshot(Long wishlistItemId) {
        Map<Long, WishlistItemSnapshot> result = getWishlistItemSnapshotUseCase.getSnapshotList(List.of(wishlistItemId));
        return result.get(wishlistItemId);
    }

    @Override
    public Map<Long, WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemIds) {
        return getWishlistItemSnapshotUseCase.getSnapshotList(wishlistItemIds);
    }
}
