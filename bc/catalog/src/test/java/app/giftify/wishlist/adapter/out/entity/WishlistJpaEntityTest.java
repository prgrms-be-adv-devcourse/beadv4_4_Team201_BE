package app.giftify.wishlist.adapter.out.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;

class WishlistJpaEntityTest {

	@Test
	@DisplayName("WishlistJpaEntity Builder 및 Getter 테스트")
	void entityTest() {
		WishlistJpaEntity entity = WishlistJpaEntity.builder()
			.memberId(10L)
			.visibility(Visibility.PUBLIC)
			.build();

		assertThat(entity.getMemberId()).isEqualTo(10L);
		assertThat(entity.getVisibility()).isEqualTo(Visibility.PUBLIC);
	}
}
