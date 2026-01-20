package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;

public interface UpdateWishlistSettingsUseCase {
    Wishlist updateSettings(UpdateSettingsCommand command);

    record UpdateSettingsCommand(
            String authSub,
            Visibility visibility
    ) {
    }
}
