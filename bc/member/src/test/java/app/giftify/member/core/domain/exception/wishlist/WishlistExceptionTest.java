package app.giftify.member.core.domain.exception.wishlist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistExceptionTest {

    @Test
    @DisplayName("WishlistNotFoundException 생성 테스트")
    void wishlistNotFoundExceptionTest() {
        WishlistNotFoundException ex1 = new WishlistNotFoundException();
        assertThat(ex1.getErrorCode()).isEqualTo(WishlistErrorCode.WISHLIST_NOT_FOUND);

        WishlistNotFoundException ex2 = new WishlistNotFoundException(1L);
        assertThat(ex2.getMessage()).contains("1");

        WishlistNotFoundException ex3 = new WishlistNotFoundException("user123");
        assertThat(ex3.getMessage()).contains("user123");
    }

    @Test
    @DisplayName("InvalidWishlistVisibilityException 생성 테스트")
    void invalidWishlistVisibilityExceptionTest() {
        InvalidWishlistVisibilityException ex = new InvalidWishlistVisibilityException("INVALID");
        assertThat(ex.getErrorCode()).isEqualTo(WishlistErrorCode.INVALID_WISHLIST_VALUE);
        assertThat(ex.getMessage()).contains("INVALID");
    }

    @Test
    @DisplayName("InvalidWishlistItemStatusException 생성 테스트")
    void invalidWishlistItemStatusExceptionTest() {
        InvalidWishlistItemStatusException ex = new InvalidWishlistItemStatusException("DRAFT");
        assertThat(ex.getErrorCode()).isEqualTo(WishlistErrorCode.INVALID_WISHLIST_ITEM_STAUTS);
        assertThat(ex.getMessage()).contains("DRAFT");
    }

    @Test
    @DisplayName("WishlistErrorCode getter 테스트")
    void wishlistErrorCodeTest() {
        WishlistErrorCode code = WishlistErrorCode.DUPLICATE_WISHLIST_ITEM;
        assertThat(code.getCode()).isEqualTo("W201");
        assertThat(code.getMessage()).isEqualTo("이미 위시리스트에 존재하는 아이템입니다.");
    }
}
