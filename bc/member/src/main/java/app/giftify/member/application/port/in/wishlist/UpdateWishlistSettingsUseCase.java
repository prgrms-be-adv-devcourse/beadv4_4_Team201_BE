package app.giftify.member.application.port.in.wishlist;

import app.giftify.member.core.domain.wishlist.Visibility;
import app.giftify.member.core.domain.wishlist.Wishlist;

public interface UpdateWishlistSettingsUseCase {
    Wishlist updateSettings(UpdateSettingsCommand command);

    record UpdateSettingsCommand(
            Long memberId,
            Visibility visibility
    ) {
    }
}
