package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;

public interface AddWishlistItemUseCase {
    WishlistItem addWishlistItem(WishlistItemAddCommand command);

    record WishlistItemAddCommand(
            String authSub,
            Long productId,
            ItemStatus itemStatus
    ) {
    }
}
