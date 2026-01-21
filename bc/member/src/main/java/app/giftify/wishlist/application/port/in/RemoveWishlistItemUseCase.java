package app.giftify.wishlist.application.port.in;

public interface RemoveWishlistItemUseCase {
    void removeWishlistItem(WishlistItemRemoveCommand command);

    record WishlistItemRemoveCommand(
            String authSub,
            Long productId
    ) {
    }
}
