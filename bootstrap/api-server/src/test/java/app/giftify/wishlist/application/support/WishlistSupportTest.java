package app.giftify.wishlist.application.support;

import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WishlistSupportTest {

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @InjectMocks
    private WishlistSupport wishlistSupport;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("위시리스트 ID로 조회 성공")
        void success() {
            // given
            Long wishlistId = 1L;
            Wishlist wishlist = Wishlist.builder()
                    .id(wishlistId)
                    .memberId(10L)
                    .visibility(Visibility.PUBLIC)
                    .build();
            given(wishlistRepositoryPort.findById(wishlistId)).willReturn(Optional.of(wishlist));

            // when
            Wishlist result = wishlistSupport.getWishlistById(wishlistId);

            // then
            assertThat(result.getId()).isEqualTo(wishlistId);
        }

        @Test
        @DisplayName("존재하지 않는 위시리스트 ID로 조회 시 예외 발생")
        void notFound() {
            // given
            Long wishlistId = 999L;
            given(wishlistRepositoryPort.findById(wishlistId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistSupport.getWishlistById(wishlistId))
                    .isInstanceOf(WishlistNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByMemberId")
    class GetByMemberId {

        @Test
        @DisplayName("멤버 ID로 위시리스트 조회 성공")
        void success() {
            // given
            Long memberId = 10L;
            Wishlist wishlist = Wishlist.builder()
                    .id(1L)
                    .memberId(memberId)
                    .visibility(Visibility.PUBLIC)
                    .build();
            given(wishlistRepositoryPort.findByMemberId(memberId)).willReturn(Optional.of(wishlist));

            // when
            Wishlist result = wishlistSupport.getWishlistByMemberId(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회 시 예외 발생")
        void notFound() {
            // given
            Long memberId = 999L;
            given(wishlistRepositoryPort.findByMemberId(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistSupport.getWishlistByMemberId(memberId))
                    .isInstanceOf(WishlistNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOrCreateByMemberId")
    class GetOrCreateByMemberId {

        @Test
        @DisplayName("이미 존재하는 경우 조회된 결과 반환")
        void alreadyExists() {
            // given
            Long memberId = 10L;
            Wishlist wishlist = Wishlist.builder()
                    .id(1L)
                    .memberId(memberId)
                    .build();
            given(wishlistRepositoryPort.findByMemberId(memberId)).willReturn(Optional.of(wishlist));

            // when
            Wishlist result = wishlistSupport.getOrCreateWishlistByMemberId(memberId);

            // then
            assertThat(result).isEqualTo(wishlist);
        }

        @Test
        @DisplayName("존재하지 않는 경우 새로 생성")
        void createNew() {
            // given
            Long memberId = 10L;
            given(wishlistRepositoryPort.findByMemberId(memberId)).willReturn(Optional.empty());
            Wishlist wishlist = Wishlist.builder()
                    .memberId(memberId)
                    .build();
            given(wishlistRepositoryPort.save(any())).willReturn(wishlist);

            // when
            Wishlist result = wishlistSupport.getOrCreateWishlistByMemberId(memberId);

            // then
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }
    }

    @Nested
    @DisplayName("getByWishlistIdAndProductId")
    class GetByWishlistIdAndProductId {

        @Test
        @DisplayName("위시리스트 ID와 상품 ID로 아이템 조회 성공")
        void success() {
            // given
            Long wishlistId = 1L;
            Long productId = 100L;
            WishlistItem wishlistItem = WishlistItem.builder()
                    .id(1L)
                    .wishlistId(wishlistId)
                    .productId(productId)
                    .wishlistItemStatus(WishlistItemStatus.PENDING)
                    .build();
            given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlistId, productId))
                    .willReturn(Optional.of(wishlistItem));

            // when
            WishlistItem result = wishlistSupport.getWishlistItemByWishlistIdAndProductId(wishlistId, productId);

            // then
            assertThat(result.getWishlistId()).isEqualTo(wishlistId);
            assertThat(result.getProductId()).isEqualTo(productId);
        }

        @Test
        @DisplayName("존재하지 않는 아이템 조회 시 예외 발생")
        void notFound() {
            // given
            Long wishlistId = 1L;
            Long productId = 999L;
            given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlistId, productId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistSupport.getWishlistItemByWishlistIdAndProductId(wishlistId, productId))
                    .isInstanceOf(WishlistItemNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getWishlistItemById")
    class GetWishlistItemById {

        @Test
        @DisplayName("위시리스트 아이템 ID로 조회 성공")
        void success() {
            // given
            Long wishlistItemId = 1L;
            WishlistItem wishlistItem = WishlistItem.builder()
                    .id(wishlistItemId)
                    .wishlistId(10L)
                    .productId(100L)
                    .wishlistItemStatus(WishlistItemStatus.PENDING)
                    .build();
            given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

            // when
            WishlistItem result = wishlistSupport.getWishlistItemById(wishlistItemId);

            // then
            assertThat(result.getId()).isEqualTo(wishlistItemId);
        }

        @Test
        @DisplayName("존재하지 않는 위시리스트 아이템 ID로 조회 시 예외 발생")
        void notFound() {
            // given
            Long wishlistItemId = 999L;
            given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> wishlistSupport.getWishlistItemById(wishlistItemId))
                    .isInstanceOf(WishlistItemNotFoundException.class);
        }
    }
}
