package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.application.support.WishlistSupport;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistItemServiceTest {

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private WishlistSupport wishlistSupport;

    @InjectMocks
    private WishlistItemService wishlistItemService;

    private static final Long MEMBER_ID = 1L;
    private static final Long WISHLIST_ID = 100L;

    @Test
    @DisplayName("위시리스트 아이템을 추가한다")
    void addWishlistItem() {
        // given
        Long productId = 1L;
        AddWishlistItemUseCase.WishlistItemAddCommand command =
                new AddWishlistItemUseCase.WishlistItemAddCommand(productId);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

        // 상품이 ACTIVE 상태임
        Product product = Mockito.mock(Product.class);
        given(product.getId()).willReturn(productId);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(productSupport.findById(productId)).willReturn(product);

        given(wishlistItemRepositoryPort.findByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(
                Optional.empty());
        given(wishlistItemRepositoryPort.save(any(WishlistItem.class))).willAnswer(
                invocation -> invocation.getArgument(0));

        // when
        WishlistItem result = wishlistItemService.addWishlistItem(MEMBER_ID, command);

        // then
        assertThat(result.getWishlistId()).isEqualTo(WISHLIST_ID);
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.PENDING);
        verify(wishlistItemRepositoryPort).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("판매 중이지 않은 상품을 추가하려 하면 예외가 발생한다")
    void addWishlistItemFailStatus() {
        // given
        Long productId = 1L;
        AddWishlistItemUseCase.WishlistItemAddCommand command =
                new AddWishlistItemUseCase.WishlistItemAddCommand(productId);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

        // 상품이 INACTIVE 상태임
        Product product = Mockito.mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.INACTIVE);
        given(productSupport.findById(productId)).willReturn(product);

        // when & then
        assertThatThrownBy(() -> wishlistItemService.addWishlistItem(MEMBER_ID, command))
                .isInstanceOf(ProductNotOnSaleException.class);
    }

    @Test
    @DisplayName("위시리스트 아이템을 제거한다")
    void removeWishlistItem() {
        // given
        Long productId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, productId);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getByMemberId(MEMBER_ID)).willReturn(wishlist);

        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlistId(WISHLIST_ID)
                .productId(productId)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(wishlistSupport.getByWishlistIdAndProductId(WISHLIST_ID, productId)).willReturn(wishlistItem);

        // when
        wishlistItemService.removeWishlistItem(command);

        // then
        verify(wishlistItemRepositoryPort).delete(wishlistItem);
    }

    @Test
    @DisplayName("존재하지 않는 아이템을 제거하려 하면 예외가 발생한다")
    void removeWishlistItemFail() {
        // given
        Long productId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, productId);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getByMemberId(MEMBER_ID)).willReturn(wishlist);
        given(wishlistSupport.getByWishlistIdAndProductId(WISHLIST_ID, productId))
                .willThrow(new WishlistItemNotFoundException());

        // when & then
        assertThatThrownBy(() -> wishlistItemService.removeWishlistItem(command))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }

    @Test
    @DisplayName("위시리스트 아이템 목록을 조회한다")
    void getWishlistItems() {
        // given
        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

        List<WishlistItem> items = List.of(
                WishlistItem.builder()
                        .wishlistId(WISHLIST_ID)
                        .productId(1L)
                        .wishlistItemStatus(WishlistItemStatus.PENDING)
                        .build(),
                WishlistItem.builder()
                        .wishlistId(WISHLIST_ID)
                        .productId(2L)
                        .wishlistItemStatus(WishlistItemStatus.PENDING)
                        .build()
        );
        given(wishlistItemRepositoryPort.findByWishlistId(WISHLIST_ID)).willReturn(items);

        // when
        List<WishlistItem> result = wishlistItemService.getWishlistItems(MEMBER_ID);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(WishlistItem::getProductId).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("위시리스트 아이템 스냅샷을 조회한다")
    void getSnapshot() {
        // given
        Long wishlistItemId = 1L;
        Long productId = 100L;
        String productName = "테스트 상품";
        int productPrice = 10000;
        Long sellerId = 5L;

        WishlistItem wishlistItem = WishlistItem.builder()
                .id(wishlistItemId)
                .wishlistId(WISHLIST_ID)
                .productId(productId)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(wishlistSupport.getWishlistItemById(wishlistItemId)).willReturn(wishlistItem);

        Product product = Mockito.mock(Product.class);
        given(product.getId()).willReturn(productId);
        given(product.getName()).willReturn(productName);
        given(product.getPrice()).willReturn(productPrice);
        given(product.getSellerId()).willReturn(sellerId);
        given(productSupport.findById(productId)).willReturn(product);

        // when
        WishlistItemSnapshot result = wishlistItemService.getSnapshot(wishlistItemId);

        // then
        assertThat(result.originalWishlistItemId()).isEqualTo(wishlistItemId);
        assertThat(result.productId()).isEqualTo(productId);
        assertThat(result.productName()).isEqualTo(productName);
        assertThat(result.productPrice()).isEqualTo(productPrice);
        assertThat(result.sellerId()).isEqualTo(sellerId);
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 아이템 스냅샷 조회 시 예외 발생")
    void getSnapshot_NotFound() {
        // given
        Long wishlistItemId = 999L;
        given(wishlistSupport.getWishlistItemById(wishlistItemId))
                .willThrow(new WishlistItemNotFoundException());

        // when & then
        assertThatThrownBy(() -> wishlistItemService.getSnapshot(wishlistItemId))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }
}
