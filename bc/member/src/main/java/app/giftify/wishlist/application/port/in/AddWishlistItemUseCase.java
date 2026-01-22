package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

public interface AddWishlistItemUseCase {
	WishlistItem addWishlistItem(WishlistItemAddCommand command);

	record WishlistItemAddCommand(
		String authSub,
		Long productId,
		WishlistItemStatus wishListItemStatus
	) {
	}
}
