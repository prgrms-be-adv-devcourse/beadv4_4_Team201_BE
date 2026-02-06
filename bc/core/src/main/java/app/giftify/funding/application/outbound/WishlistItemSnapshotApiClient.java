package app.giftify.funding.application.outbound;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface WishlistItemSnapshotApiClient {

    @PostExchange("/api/internal/wishlist/items/snapshots")
    List<WishlistItemSnapshot> getSnapshotList(
            @RequestBody List<Long> wishlistItemIds
    );
}
