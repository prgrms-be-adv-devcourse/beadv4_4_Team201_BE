package app.giftify.orderDemo.adapter.outbound.client;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;
import java.util.Map;

@HttpExchange(url = "/api/internal/wishlist")
public interface WishlistClient {

    @GetExchange(url = "/items/snapshots")
    Map<Long, WishlistItemSnapshot> getSnapshotList(@RequestBody List<Long> wishlistItemIds);
}
