package app.giftify.wishlist.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

@ExtendWith(MockitoExtension.class)
class PublicWishlistServiceTest {

	@Mock
	private WishlistRepositoryPort wishlistRepositoryPort;

	@Mock
	private WishlistItemRepositoryPort wishlistItemRepositoryPort;

	@InjectMocks
	private PublicWishlistService publicWishlistService;

	@Test
	@DisplayName("PUBLIC 위시리스트의 아이템 목록을 반환한다")
	void getPublicWishlistItems_returnsItems() {
		Long memberId = 1L;
		Wishlist wishlist = Wishlist.builder().id(10L).memberId(memberId).visibility(Visibility.PUBLIC).build();
		WishlistItem item = WishlistItem.builder()
			.id(100L).wishlistId(10L).productId(5L)
			.wishlistItemStatus(WishlistItemStatus.PENDING).addedAt(LocalDateTime.now())
			.build();

		given(wishlistRepositoryPort.findByMemberIdAndVisibility(memberId, Visibility.PUBLIC))
			.willReturn(Optional.of(wishlist));
		given(wishlistItemRepositoryPort.findByWishlistId(10L))
			.willReturn(List.of(item));

		List<WishlistItem> result = publicWishlistService.getPublicWishlistItems(memberId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getProductId()).isEqualTo(5L);
	}

	@Test
	@DisplayName("PUBLIC이 아닌 위시리스트는 빈 리스트를 반환한다")
	void getPublicWishlistItems_nonPublic_returnsEmpty() {
		Long memberId = 2L;

		given(wishlistRepositoryPort.findByMemberIdAndVisibility(memberId, Visibility.PUBLIC))
			.willReturn(Optional.empty());

		List<WishlistItem> result = publicWishlistService.getPublicWishlistItems(memberId);

		assertThat(result).isEmpty();
		then(wishlistItemRepositoryPort).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("memberIds 중 PUBLIC 위시리스트 보유자만 필터링한다")
	void findPublicWishlists_filtersPublicOnly() {
		List<Long> memberIds = List.of(1L, 2L, 3L);
		Wishlist w1 = Wishlist.builder().id(10L).memberId(1L).visibility(Visibility.PUBLIC).build();
		Wishlist w3 = Wishlist.builder().id(30L).memberId(3L).visibility(Visibility.PUBLIC).build();

		given(wishlistRepositoryPort.findByMemberIdInAndVisibility(memberIds, Visibility.PUBLIC))
			.willReturn(List.of(w1, w3));

		List<Wishlist> result = publicWishlistService.findPublicWishlists(memberIds);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Wishlist::getMemberId).containsExactly(1L, 3L);
	}

	@Test
	@DisplayName("빈 memberIds 리스트는 빈 결과를 반환한다")
	void findPublicWishlists_emptyInput_returnsEmpty() {
		List<Wishlist> result = publicWishlistService.findPublicWishlists(Collections.emptyList());

		assertThat(result).isEmpty();
		then(wishlistRepositoryPort).shouldHaveNoInteractions();
	}
}
