package app.giftify.wishlist.core.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.member.domain.member.Member;

class WishlistItemTest {

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
	@DisplayName("Builder를 사용하여 WishlistItem 객체를 성공적으로 생성한다")
	void createWishlistItemWithBuilder() {
		// given
		Long id = 1L;
		Long wishlistId = wishlist.getId();
		Long productId = 200L;
		WishlistItemStatus wishlistItemStatus = WishlistItemStatus.PENDING;

		// when
		WishlistItem wishlistItem = WishlistItem.builder()
			.id(id)
			.wishlistId(wishlistId)
			.productId(productId)
			.wishlistItemStatus(wishlistItemStatus)
			.addedAt(LocalDate.now())
			.build();

		// then
		assertThat(wishlistItem.getId()).isEqualTo(id);
		assertThat(wishlistItem.getWishlistId()).isEqualTo(wishlistId);
		assertThat(wishlistItem.getProductId()).isEqualTo(productId);
		assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(wishlistItemStatus);
		assertThat(wishlistItem.getAddedAt()).isNotNull();
	}

	@Test
	@DisplayName("유효하지 않은 wishlistId 또는 productId로 생성 시 예외가 발생한다")
	void createWishlistItemWithInvalidIds() {
		assertThatThrownBy(() -> WishlistItem.builder()
			.wishlistId(null)
			.productId(1L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 위시리스트 ID 입니다.");

		assertThatThrownBy(() -> WishlistItem.builder()
			.wishlistId(0L)
			.productId(1L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 위시리스트 ID 입니다.");

		assertThatThrownBy(() -> WishlistItem.builder()
			.wishlistId(wishlist.getId())
			.productId(0L)
			.wishlistItemStatus(WishlistItemStatus.PENDING)
			.build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 상품 ID입니다.");
	}
}
