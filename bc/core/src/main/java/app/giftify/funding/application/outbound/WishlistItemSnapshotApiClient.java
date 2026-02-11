package app.giftify.funding.application.outbound;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Map;

@HttpExchange
public interface WishlistItemSnapshotApiClient {

    @GetExchange("/api/internal/wishlist/items/{wishlistItemId}/snapshot")
    WishlistItemSnapshot getSnapshot(
            @PathVariable Long wishlistItemId
    );

    @PostExchange("/api/internal/wishlist/items/snapshots")
    Map<Long, WishlistItemSnapshot> getSnapshotList(@RequestBody List<Long> wishlistItemIds);
}
