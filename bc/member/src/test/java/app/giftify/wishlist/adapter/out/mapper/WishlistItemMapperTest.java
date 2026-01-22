package app.giftify.wishlist.adapter.out.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistItemMapper;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

class WishlistItemMapperTest {

	@Test
	@DisplayName("WishlistItem 도메인을 Entity로 변환 테스트")
	void toEntityTest() {
		WishlistItem domain = WishlistItem.builder()
			.id(1L)
			.authSub("user123")
			.productId(100L)
			.WishlistItemStatus(WishlistItemStatus.PENDING)
			.build();

		WishlistItemJpaEntity entity = WishlistItemMapper.toEntity(domain);

		assertThat(entity.getAuthSub()).isEqualTo(domain.getAuthSub());
		assertThat(entity.getProductId()).isEqualTo(domain.getProductId());
		assertThat(entity.getWishlistItemStatus()).isEqualTo(domain.getWishlistItemStatus());
	}

	@Test
	@DisplayName("WishlistItem Entity를 도메인으로 변환 테스트")
	void toDomainTest() {
		WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
			.authSub("user123")
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();

		WishlistItem domain = WishlistItemMapper.toDomain(entity);

		assertThat(domain.getId()).isEqualTo(entity.getId());
		assertThat(domain.getAuthSub()).isEqualTo(entity.getAuthSub());
		assertThat(domain.getProductId()).isEqualTo(entity.getProductId());
		assertThat(domain.getWishlistItemStatus()).isEqualTo(entity.getWishlistItemStatus());
	}
}
