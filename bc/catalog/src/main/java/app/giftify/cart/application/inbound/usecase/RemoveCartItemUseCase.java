package app.giftify.cart.application.inbound.usecase;

import java.util.List;

public interface RemoveCartItemUseCase {
	void removeItems(Long memberId, List<Long> wishlistItemIds);
}
