package app.giftify.funding.application.outbound;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface WishlistItemSnapshotApiClient {

    @GetExchange("/api/wishlist/items/{wishlistItemId}/snapshot")
    WishlistItemSnapshot getSnapshot(
            @PathVariable Long wishlistItemId
    );
}
