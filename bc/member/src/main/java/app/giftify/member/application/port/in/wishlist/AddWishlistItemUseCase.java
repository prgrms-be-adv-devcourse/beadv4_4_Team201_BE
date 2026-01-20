package app.giftify.member.application.port.in.wishlist;

import app.giftify.member.core.domain.wishlist.ItemStatus;
import app.giftify.member.core.domain.wishlist.WishlistItem;

public interface AddWishlistItemUseCase {
    WishlistItem addWishlistItem(WishlistItemAddCommand command);

    record WishlistItemAddCommand(
            String authSub,
            Long productId,
            ItemStatus itemStatus
    ) {
    }
}
