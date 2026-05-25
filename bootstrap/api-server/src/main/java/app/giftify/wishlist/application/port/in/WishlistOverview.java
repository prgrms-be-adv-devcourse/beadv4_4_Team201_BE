package app.giftify.wishlist.application.port.in;

import app.giftify.support.common.api.paging.Page;
import app.giftify.wishlist.core.domain.Wishlist;

public record WishlistOverview(
        Wishlist wishlist,
        String ownerNickname,
        Page<WishlistItemDetail> itemDetails
) {
}
