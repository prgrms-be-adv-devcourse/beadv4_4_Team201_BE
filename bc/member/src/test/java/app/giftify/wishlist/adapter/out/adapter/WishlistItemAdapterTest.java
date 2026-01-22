package app.giftify.wishlist.adapter.out.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.member.core.domain.member.Member;
import app.giftify.wishlist.adapter.out.jpa.adapter.WishlistItemAdapter;
import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistItemJpaRepository;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

@ExtendWith(MockitoExtension.class)
class WishlistItemAdapterTest {

	@Mock
	private WishlistItemJpaRepository wishlistItemRepository;

	@InjectMocks
	private WishlistItemAdapter wishlistItemAdapter;

	private Member member;
	private Wishlist wishlist;

	@BeforeEach
	void setUp() {
		// Member 생성
		member = Member.builder()
			.id(1L)
			.email("test@example.com")
			.nickname("tester")
			.birthday(LocalDate.of(1990, 1, 1))
			.authSub("auth0|123")
			.build();

		// Wishlist 생성
		wishlist = Wishlist.builder()
			.id(100L)
			.memberId(member.getId())
			.visibility(Visibility.PUBLIC)
			.build();
	}

	@Test
	@DisplayName("wishlistId와 productId로 위시리스트 아이템 조회 테스트")
	void findByWishlistIdAndProductIdTest() {
		Long wishlistId = wishlist.getId();
		Long productId = 100L;
		WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
			.wishlistId(wishlistId)
			.productId(productId)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		given(wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId)).willReturn(
			Optional.of(entity));

		Optional<WishlistItem> result = wishlistItemAdapter.findByWishlistIdAndProductId(wishlistId, productId);

		assertThat(result).isPresent();
		assertThat(result.get().getProductId()).isEqualTo(productId);
	}

	@Test
	@DisplayName("wishlistId로 모든 위시리스트 아이템 조회 테스트")
	void findByWishlistIdTest() {
		Long wishlistId = wishlist.getId();
		WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
			.wishlistId(wishlistId)
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		given(wishlistItemRepository.findByWishlistId(wishlistId)).willReturn(List.of(entity));

		List<WishlistItem> result = wishlistItemAdapter.findByWishlistId(wishlistId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getWishlistId()).isEqualTo(wishlistId);
	}

	@Test
	@DisplayName("위시리스트 아이템 저장 테스트")
	void saveTest() {
		Long wishlistId = wishlist.getId();
		WishlistItem domain = WishlistItem.builder()
			.wishlistId(wishlistId)
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		WishlistItemJpaEntity entity = WishlistItemJpaEntity.builder()
			.wishlistId(wishlistId)
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();
		given(wishlistItemRepository.save(any(WishlistItemJpaEntity.class))).willReturn(entity);

		WishlistItem result = wishlistItemAdapter.save(domain);

		assertThat(result.getProductId()).isEqualTo(domain.getProductId());
		verify(wishlistItemRepository).save(any(WishlistItemJpaEntity.class));
	}

	@Test
	@DisplayName("wishlistId와 productId로 삭제 테스트")
	void deleteByWishlistIdAndProductIdTest() {
		Long wishlistId = wishlist.getId();
		Long productId = 100L;

		wishlistItemAdapter.deleteByWishlistIdAndProductId(wishlistId, productId);

		verify(wishlistItemRepository).deleteByWishlistIdAndProductId(wishlistId, productId);
	}

	@Test
	@DisplayName("도메인 객체로 삭제 테스트")
	void deleteTest() {
		Long wishlistId = wishlist.getId();
		Long itemId = 1L;
		WishlistItem domain = WishlistItem.builder()
			.id(itemId)
			.wishlistId(wishlistId)
			.productId(100L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build();

		wishlistItemAdapter.delete(domain);

		verify(wishlistItemRepository).deleteById(itemId);
	}

	@Test
	@DisplayName("카운트 테스트")
	void countTest() {
		given(wishlistItemRepository.count()).willReturn(10L);

		Long count = wishlistItemAdapter.count();

		assertThat(count).isEqualTo(10L);
	}
}
