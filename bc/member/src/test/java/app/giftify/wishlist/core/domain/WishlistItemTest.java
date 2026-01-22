package app.giftify.wishlist.core.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WishlistItemTest {

	@Test
	@DisplayName("Builder를 사용하여 WishlistItem 객체를 성공적으로 생성한다")
	void createWishlistItemWithBuilder() {
		// given
		Long id = 1L;
		String authSub = "auth0|12345";
		Long productId = 200L;
		WishlistItemStatus wishlistItemStatus = WishlistItemStatus.PENDING;

		// when
		WishlistItem wishlistItem = WishlistItem.builder()
			.id(id)
			.authSub(authSub)
			.productId(productId)
			.WishlistItemStatus(wishlistItemStatus)
			.addedAt(LocalDate.now())
			.build();

		// then
		assertThat(wishlistItem.getId()).isEqualTo(id);
		assertThat(wishlistItem.getAuthSub()).isEqualTo(authSub);
		assertThat(wishlistItem.getProductId()).isEqualTo(productId);
		assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(wishlistItemStatus);
		assertThat(wishlistItem.getAddedAt()).isNotNull();
	}

	@Test
	@DisplayName("유효하지 않은 authSub 또는 productId로 생성 시 예외가 발생한다")
	void createWishlistItemWithInvalidIds() {
		assertThatThrownBy(() -> WishlistItem.builder().authSub(null).productId(1L).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 authSub 입니다.");

		assertThatThrownBy(() -> WishlistItem.builder().authSub(" ").productId(1L).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 authSub 입니다.");

		assertThatThrownBy(() -> WishlistItem.builder().authSub("auth0|123").productId(0L).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 상품 ID입니다.");
	}
}
