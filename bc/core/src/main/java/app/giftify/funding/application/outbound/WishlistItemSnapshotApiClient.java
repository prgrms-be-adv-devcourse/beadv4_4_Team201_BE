package app.giftify.funding.application.outbound;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;

@HttpExchange
public interface WishlistItemSnapshotApiClient {

    @GetExchange("/api/internal/wishlist/items/{wishlistItemId}/snapshot")
    WishlistItemSnapshot getSnapshot(
            @PathVariable Long wishlistItemId
    );

    @GetExchange("/api/internal/wishlist/items/snapshots")
    List<WishlistItemSnapshot> getSnapshotList(@RequestBody List<Long> wishlistItemIds);
}
