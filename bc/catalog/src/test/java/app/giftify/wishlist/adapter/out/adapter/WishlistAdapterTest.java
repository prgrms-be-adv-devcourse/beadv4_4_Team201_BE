package app.giftify.wishlist.adapter.out.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.wishlist.adapter.out.jpa.adapter.WishlistAdapter;
import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistJpaRepository;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;

@ExtendWith(MockitoExtension.class)
class WishlistAdapterTest {

	@Mock
	private WishlistJpaRepository wishlistRepository;

	@InjectMocks
	private WishlistAdapter wishlistAdapter;

	@Test
	@DisplayName("memberId로 위시리스트 조회 테스트")
	void findByMemberIdTest() {
		Long memberId = 10L;
		WishlistJpaEntity entity = WishlistJpaEntity.builder()
			.memberId(memberId)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepository.findByMemberId(memberId)).willReturn(Optional.of(entity));

		Optional<Wishlist> result = wishlistAdapter.findByMemberId(memberId);

		assertThat(result).isPresent();
		assertThat(result.get().getMemberId()).isEqualTo(memberId);
	}

	// @Test
	// @DisplayName("authSub로 위시리스트 조회 테스트")
	// void findByAuthSubTest() {
	//     String authSub = "user123";
	//     WishlistJpaEntity entity = WishlistJpaEntity.builder()
	//             .memberId(10L)
	//             .visibility(Visibility.PUBLIC)
	//             .build();
	//     given(wishlistRepository.findBy(authSub)).willReturn(Optional.of(entity));
	//
	//     Optional<Wishlist> result = wishlistAdapter.findByAuthSub(authSub);
	//
	//     assertThat(result).isPresent();
	//     assertThat(result.get().getAuthSub()).isEqualTo(authSub);
	// }

	@Test
	@DisplayName("위시리스트 저장 테스트")
	void saveTest() {
		Wishlist domain = Wishlist.builder()
			.id(1L)
			.memberId(10L)
			.visibility(Visibility.PUBLIC)
			.build();
		WishlistJpaEntity entity = WishlistJpaEntity.builder()
			.memberId(10L)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepository.save(any(WishlistJpaEntity.class))).willReturn(entity);

		Wishlist result = wishlistAdapter.save(domain);

		assertThat(result.getMemberId()).isEqualTo(domain.getMemberId());
		verify(wishlistRepository).save(any(WishlistJpaEntity.class));
	}
}
