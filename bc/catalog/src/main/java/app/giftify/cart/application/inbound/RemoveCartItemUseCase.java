package app.giftify.cart.application.inbound;

import app.giftify.shared.domain.type.TargetType;

public interface RemoveCartItemUseCase {
	void removeItem(Long memberId, TargetType targetType, Long targetId);
}
