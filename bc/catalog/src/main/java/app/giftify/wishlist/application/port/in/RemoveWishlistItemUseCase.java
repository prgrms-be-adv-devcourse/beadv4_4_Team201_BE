package app.giftify.wishlist.application.port.in;

public interface RemoveWishlistItemUseCase {
	void removeWishlistItem(WishlistItemRemoveCommand command);

	record WishlistItemRemoveCommand(
		Long memberId,
		Long productId
	) {
	}
}
