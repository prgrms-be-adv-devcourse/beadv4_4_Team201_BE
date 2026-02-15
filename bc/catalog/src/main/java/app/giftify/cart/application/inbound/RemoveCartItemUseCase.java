package app.giftify.cart.application.inbound;

import app.giftify.shared.domain.type.TargetType;

import java.util.List;

public interface RemoveCartItemUseCase {
	void removeItems(Long memberId, TargetType targetType, List<Long> targetIds);
}
