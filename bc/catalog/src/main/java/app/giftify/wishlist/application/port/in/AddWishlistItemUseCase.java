package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.WishlistItem;

public interface AddWishlistItemUseCase {
    WishlistItem addWishlistItem(Long memberId, WishlistItemAddCommand command);

    record WishlistItemAddCommand(
            Long productId
    ) {
    }
}
