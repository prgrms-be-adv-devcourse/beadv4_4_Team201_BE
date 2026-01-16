package app.giftify.in.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * WishlistItem 생성 이벤트 (임시 정의)
 * TODO: Member BC에서 실제 이벤트를 정의하고, 해당 이벤트를 import하여 사용
 * 이 파일은 Member BC 구현 후 삭제 예정
 */
@Getter
@RequiredArgsConstructor
public class WishlistItemCreatedEvent {
	private final Long wishlistItemId;
	private final Long productId;
}

