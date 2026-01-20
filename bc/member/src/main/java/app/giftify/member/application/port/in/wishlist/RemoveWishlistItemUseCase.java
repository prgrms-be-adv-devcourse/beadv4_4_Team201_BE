package app.giftify.member.application.port.in.wishlist;

public interface RemoveWishlistItemUseCase {
    void removeWishlistItem(WishlistItemRemoveCommand command);

    record WishlistItemRemoveCommand(
            String authSub,
            Long productId
    ) {
    }
}
