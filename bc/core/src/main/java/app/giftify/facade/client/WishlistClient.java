package app.giftify.facade.client;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/internal/wishlist")
public interface WishlistClient {

    @GetExchange(url = "/items/{wishlistItemId}/snapshot")
    WishlistItemSnapshot getWishlistItemSnapshot(@PathVariable("wishlistItemId") Long wishlistId);
}
