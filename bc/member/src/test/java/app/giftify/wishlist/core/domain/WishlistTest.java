package app.giftify.wishlist.core.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WishlistTest {

	@Test
	@DisplayName("Builder를 사용하여 Wishlist 객체를 성공적으로 생성한다")
	void createWishlistWithBuilder() {
		// given
		Long id = 1L;
		Long memberId = 100L;
		Visibility visibility = Visibility.PUBLIC;
		LocalDate createdAt = LocalDate.now();

		// when
		Wishlist wishlist = Wishlist.builder()
			.id(id)
			.memberId(memberId)
			.visibility(visibility)
			.createdAt(createdAt)
			.build();

		// then
		assertThat(wishlist.getId()).isEqualTo(id);
		assertThat(wishlist.getMemberId()).isEqualTo(memberId);
		assertThat(wishlist.getVisibility()).isEqualTo(visibility);
		assertThat(wishlist.getCreatedAt()).isEqualTo(createdAt);
	}

	@Test
	@DisplayName("유효하지 않은 memberId로 Wishlist 생성 시 예외가 발생한다")
	void createWishlistWithInvalidMemberId() {
		assertThatThrownBy(() -> Wishlist.builder().memberId(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 회원 ID입니다.");

		assertThatThrownBy(() -> Wishlist.builder().memberId(0L).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("유효하지 않은 회원 ID입니다.");
	}

	@Test
	@DisplayName("위시리스트의 공개 여부를 변경할 수 있다")
	void changeVisibility() {
		// given
		Wishlist wishlist = Wishlist.builder()
			.memberId(1L)
			.visibility(Visibility.PRIVATE)
			.build();

		// when
		wishlist.changeVisibility(Visibility.PUBLIC);

		// then
		assertThat(wishlist.getVisibility()).isEqualTo(Visibility.PUBLIC);
	}
}
