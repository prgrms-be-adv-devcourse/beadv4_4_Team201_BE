package app.giftify.wishlist.application.port.in;

import jakarta.validation.constraints.NotNull;

public interface AddWishlistItemUseCase {
    void addWishlistItem(Long memberId, WishlistItemAddCommand command);

    record WishlistItemAddCommand(
            @NotNull
            Long productId
    ) {
    }
}
