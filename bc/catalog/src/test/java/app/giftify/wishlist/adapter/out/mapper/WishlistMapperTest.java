package app.giftify.wishlist.adapter.out.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistMapper;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;

class WishlistMapperTest {

	@Test
	@DisplayName("Wishlist 도메인을 Entity로 변환 테스트")
	void toEntityTest() {
		Wishlist domain = Wishlist.builder()
			.id(1L)
			.memberId(10L)
			.visibility(Visibility.PUBLIC)
			.build();

		WishlistJpaEntity entity = WishlistMapper.toEntity(domain);

		assertThat(entity.getMemberId()).isEqualTo(domain.getMemberId());
		assertThat(entity.getVisibility()).isEqualTo(domain.getVisibility());
	}

	@Test
	@DisplayName("Wishlist Entity를 도메인으로 변환 테스트")
	void toDomainTest() {
		WishlistJpaEntity entity = WishlistJpaEntity.builder()
			.memberId(10L)
			.visibility(Visibility.PRIVATE)
			.build();

		Wishlist domain = WishlistMapper.toDomain(entity);

		assertThat(domain.getId()).isEqualTo(entity.getId());
		assertThat(domain.getMemberId()).isEqualTo(entity.getMemberId());
		assertThat(domain.getVisibility()).isEqualTo(entity.getVisibility());
	}
}
