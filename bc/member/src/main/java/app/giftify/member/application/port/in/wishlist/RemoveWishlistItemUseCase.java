package app.giftify.member.application.port.in.wishlist;

public interface RemoveWishlistItemUseCase {
    void removeWishlistItem(WishlistItemRemoveCommand command);

    record WishlistItemRemoveCommand(
            Long wishlistId,
            Long productId
    ) {
    }
}
