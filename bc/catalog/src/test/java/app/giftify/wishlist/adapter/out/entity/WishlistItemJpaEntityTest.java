package app.giftify.wishlist.adapter.out.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

class WishlistItemJpaEntityTest {

	@Test
	@DisplayName("WishlistItemJpaEntity Builder 및 Getter 테스트")
	void entityTest() {
		WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
			.wishlistId(1L)
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();

		assertThat(entity.getWishlistId()).isEqualTo(1L);
		assertThat(entity.getProductId()).isEqualTo(100L);
		assertThat(entity.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.PENDING);
	}
}
