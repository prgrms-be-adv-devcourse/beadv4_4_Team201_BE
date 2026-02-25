package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.in.WishlistItemDetail;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.application.support.WishlistSupport;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.WishlistNotAccessibleException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Mock
    private FriendshipVerificationPort friendshipVerificationPort;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private WishlistSupport wishlistSupport;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private static final Long MEMBER_ID = 1L;
    private static final Long TARGET_MEMBER_ID = 2L;
    private static final Long WISHLIST_ID = 100L;
    private static final Long PRODUCT_ID = 200L;

    @Test
    @DisplayName("memberId로 위시리스트를 조회한다")
    void getOrCreateWishlistByMemberId() {
        // given
        Wishlist wishlist = Wishlist.builder()
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getOrCreateWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);

        // when
        Wishlist result = wishlistService.getOrCreateWishlistByMemberId(MEMBER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
    }

    @Test
    @DisplayName("위시리스트 설정을 변경한다")
    void updateSettings() {
        // given
        Visibility newVisibility = Visibility.PUBLIC;
        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
                new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(MEMBER_ID, newVisibility);

        Wishlist wishlist = Wishlist.builder()
                .memberId(MEMBER_ID)
                .visibility(Visibility.PRIVATE)
                .build();
        given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));
        given(wishlistRepositoryPort.save(any(Wishlist.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Wishlist result = wishlistService.updateSettings(command);

        // then
        assertThat(result.getVisibility()).isEqualTo(newVisibility);
        verify(wishlistRepositoryPort).save(wishlist);
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트의 설정을 변경하려 하면 예외가 발생한다")
    void updateSettingsFail() {
        // given
        Long notFoundMemberId = 999L;
        UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
                new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(notFoundMemberId, Visibility.PUBLIC);

        given(wishlistRepositoryPort.findByMemberId(notFoundMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wishlistService.updateSettings(command))
                .isInstanceOf(WishlistNotFoundException.class);
    }

    @Nested
    @DisplayName("getMyWishlistItemDetails - 내 위시리스트 아이템 조회")
    class GetMyWishlistItemDetailsTest {

        @Test
        @DisplayName("내 위시리스트 아이템을 상품 정보와 함께 조회한다")
        void getMyWishlistItemDetails() {
            // given
            Wishlist wishlist = Wishlist.builder()
                    .id(WISHLIST_ID)
                    .memberId(MEMBER_ID)
                    .visibility(Visibility.PRIVATE)
                    .build();
            WishlistItem item = WishlistItem.builder()
                    .id(1L)
                    .wishlistId(WISHLIST_ID)
                    .productId(PRODUCT_ID)
                    .wishlistItemStatus(WishlistItemStatus.PENDING)
                    .build();
            Product product = Product.builder()
                    .id(PRODUCT_ID)
                    .sellerId(99L)
                    .name("테스트 상품")
                    .description("설명")
                    .price(10000)
                    .stock(5)
                    .status(ProductStatus.ACTIVE)
                    .imageKey("img.jpg")
                    .build();

            given(wishlistSupport.getWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);
            given(wishlistItemRepositoryPort.findByWishlistId(WISHLIST_ID)).willReturn(List.of(item));
            given(productSupport.findAllById(List.of(PRODUCT_ID))).willReturn(List.of(product));
            given(memberRepository.findAllById(List.of(99L))).willReturn(List.of(new Member(99L, "테스트판매자")));

            // when
            List<WishlistItemDetail> result = wishlistService.getMyWishlistItemDetails(MEMBER_ID);

            // then
            assertThat(result).hasSize(1);
            WishlistItemDetail detail = result.get(0);
            assertThat(detail.productName()).isEqualTo("테스트 상품");
            assertThat(detail.price()).isEqualTo(10000);
            assertThat(detail.imageKey()).isEqualTo("img.jpg");
            assertThat(detail.isActive()).isTrue();
            assertThat(detail.isSoldout()).isFalse();
            assertThat(detail.sellerNickname()).isEqualTo("테스트판매자");
        }

        @Test
        @DisplayName("위시리스트 아이템이 없으면 빈 리스트를 반환한다")
        void getMyWishlistItemDetails_empty() {
            // given
            Wishlist wishlist = Wishlist.builder()
                    .id(WISHLIST_ID)
                    .memberId(MEMBER_ID)
                    .visibility(Visibility.PRIVATE)
                    .build();

            given(wishlistSupport.getWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);
            given(wishlistItemRepositoryPort.findByWishlistId(WISHLIST_ID)).willReturn(Collections.emptyList());

            // when
            List<WishlistItemDetail> result = wishlistService.getMyWishlistItemDetails(MEMBER_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getWishlistItemDetails - 타인의 위시리스트 아이템 조회")
    class GetWishlistItemDetailsTest {

        private Wishlist createWishlist(Long memberId, Visibility visibility) {
            return Wishlist.builder()
                    .id(WISHLIST_ID)
                    .memberId(memberId)
                    .visibility(visibility)
                    .build();
        }

        private void stubItemsAndProducts() {
            WishlistItem item = WishlistItem.builder()
                    .id(1L)
                    .wishlistId(WISHLIST_ID)
                    .productId(PRODUCT_ID)
                    .wishlistItemStatus(WishlistItemStatus.PENDING)
                    .build();
            Product product = Product.builder()
                    .id(PRODUCT_ID)
                    .sellerId(99L)
                    .name("테스트 상품")
                    .description("설명")
                    .price(10000)
                    .stock(5)
                    .status(ProductStatus.ACTIVE)
                    .imageKey("img.jpg")
                    .build();
            given(wishlistItemRepositoryPort.findByWishlistId(WISHLIST_ID)).willReturn(List.of(item));
            given(productSupport.findAllById(List.of(PRODUCT_ID))).willReturn(List.of(product));
            given(memberRepository.findAllById(List.of(99L))).willReturn(List.of(new Member(99L, "테스트판매자")));
        }

        @Test
        @DisplayName("본인 위시리스트는 PRIVATE이어도 조회할 수 있다")
        void ownerCanAccessPrivateWishlist() {
            // given
            Wishlist wishlist = createWishlist(MEMBER_ID, Visibility.PRIVATE);
            given(wishlistSupport.getWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);
            stubItemsAndProducts();

            // when
            List<WishlistItemDetail> result = wishlistService.getWishlistItemDetails(MEMBER_ID, MEMBER_ID);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("비로그인 사용자는 PUBLIC 위시리스트를 조회할 수 있다")
        void anonymousCanAccessPublicWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.PUBLIC);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);
            stubItemsAndProducts();

            // when
            List<WishlistItemDetail> result = wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, null);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("비로그인 사용자는 FRIENDS_ONLY 위시리스트를 조회할 수 없다")
        void anonymousCannotAccessFriendsOnlyWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.FRIENDS_ONLY);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);

            // when & then
            assertThatThrownBy(() -> wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, null))
                    .isInstanceOf(WishlistNotAccessibleException.class);
        }

        @Test
        @DisplayName("비로그인 사용자는 PRIVATE 위시리스트를 조회할 수 없다")
        void anonymousCannotAccessPrivateWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.PRIVATE);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);

            // when & then
            assertThatThrownBy(() -> wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, null))
                    .isInstanceOf(WishlistNotAccessibleException.class);
        }

        @Test
        @DisplayName("친구는 FRIENDS_ONLY 위시리스트를 조회할 수 있다")
        void friendCanAccessFriendsOnlyWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.FRIENDS_ONLY);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);
            given(friendshipVerificationPort.areFriends(MEMBER_ID, TARGET_MEMBER_ID)).willReturn(true);
            stubItemsAndProducts();

            // when
            List<WishlistItemDetail> result = wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, MEMBER_ID);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("친구는 PUBLIC 위시리스트를 조회할 수 있다")
        void friendCanAccessPublicWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.PUBLIC);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);
            given(friendshipVerificationPort.areFriends(MEMBER_ID, TARGET_MEMBER_ID)).willReturn(true);
            stubItemsAndProducts();

            // when
            List<WishlistItemDetail> result = wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, MEMBER_ID);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("비친구 로그인 사용자는 FRIENDS_ONLY 위시리스트를 조회할 수 없다")
        void nonFriendCannotAccessFriendsOnlyWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.FRIENDS_ONLY);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);
            given(friendshipVerificationPort.areFriends(MEMBER_ID, TARGET_MEMBER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, MEMBER_ID))
                    .isInstanceOf(WishlistNotAccessibleException.class);
        }

        @Test
        @DisplayName("비친구 로그인 사용자는 PRIVATE 위시리스트를 조회할 수 없다")
        void nonFriendCannotAccessPrivateWishlist() {
            // given
            Wishlist wishlist = createWishlist(TARGET_MEMBER_ID, Visibility.PRIVATE);
            given(wishlistSupport.getWishlistByMemberId(TARGET_MEMBER_ID)).willReturn(wishlist);
            given(friendshipVerificationPort.areFriends(MEMBER_ID, TARGET_MEMBER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> wishlistService.getWishlistItemDetails(TARGET_MEMBER_ID, MEMBER_ID))
                    .isInstanceOf(WishlistNotAccessibleException.class);
        }
    }
}
