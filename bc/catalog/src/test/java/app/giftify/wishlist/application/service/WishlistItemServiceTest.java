package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductNotActiveException;
import app.giftify.product.domain.exception.ProductOutOfStockException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.wishlist.WishlistItemRemovedEvent;
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
import app.giftify.wishlist.core.domain.exception.NotWishlistOwnerException;
import app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
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

    @Mock
    EventPublisher eventPublisher;

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
        given(wishlistSupport.getOrCreateWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);

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
        given(wishlistSupport.getOrCreateWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);

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
        Long wishlistItemId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, wishlistItemId);

        WishlistItem wishlistItem = WishlistItem.builder()
                .id(wishlistItemId)
                .wishlistId(WISHLIST_ID)
                .productId(100L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(wishlistSupport.getWishlistItemById(wishlistItemId)).willReturn(wishlistItem);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getWishlistById(WISHLIST_ID)).willReturn(wishlist);

        // when
        wishlistItemService.removeWishlistItem(command);
        ArgumentCaptor<WishlistItemRemovedEvent> captor =
                ArgumentCaptor.forClass(WishlistItemRemovedEvent.class);
        verify(eventPublisher).publish(captor.capture());

        // then
        verify(wishlistItemRepositoryPort).delete(wishlistItem);
    }

    @Test
    @DisplayName("존재하지 않는 아이템을 제거하려 하면 예외가 발생한다")
    void removeWishlistItemFail() {
        // given
        Long wishlistItemId = 1L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(MEMBER_ID, wishlistItemId);

        given(wishlistSupport.getWishlistItemById(wishlistItemId))
                .willThrow(new WishlistItemNotFoundException());

        // when & then
        assertThatThrownBy(() -> wishlistItemService.removeWishlistItem(command))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }

    @Test
    @DisplayName("본인의 위시리스트 항목이 아니면 제거 시 예외가 발생한다")
    void removeWishlistItemFailNotOwner() {
        // given
        Long wishlistItemId = 1L;
        Long otherMemberId = 999L;
        RemoveWishlistItemUseCase.WishlistItemRemoveCommand command =
                new RemoveWishlistItemUseCase.WishlistItemRemoveCommand(otherMemberId, wishlistItemId);

        WishlistItem wishlistItem = WishlistItem.builder()
                .id(wishlistItemId)
                .wishlistId(WISHLIST_ID)
                .productId(100L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(wishlistSupport.getWishlistItemById(wishlistItemId)).willReturn(wishlistItem);

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID) // 실제 소유자 (1L)
                .build();
        given(wishlistSupport.getWishlistById(WISHLIST_ID)).willReturn(wishlist);

        // when & then
        assertThatThrownBy(() -> wishlistItemService.removeWishlistItem(command))
                .isInstanceOf(NotWishlistOwnerException.class);
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
        given(wishlistSupport.getOrCreateWishlistByMemberId(MEMBER_ID)).willReturn(wishlist);

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
    void getSnapshotList() {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L);

        WishlistItem wishlistItem1 = WishlistItem.builder()
                .id(1L)
                .wishlistId(WISHLIST_ID)
                .productId(100L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        WishlistItem wishlistItem2 = WishlistItem.builder()
                .id(2L)
                .wishlistId(WISHLIST_ID)
                .productId(101L)
                .wishlistItemStatus(WishlistItemStatus.PENDING)
                .build();
        given(wishlistSupport.getWishlistItemListById(wishlistItemIds))
                .willReturn(List.of(wishlistItem1, wishlistItem2));

        Product product1 = Mockito.mock(Product.class);
        given(product1.getId()).willReturn(100L);
        given(product1.getName()).willReturn("테스트 상품1");
        given(product1.getPrice()).willReturn(10000);
        given(product1.getSellerId()).willReturn(5L);

        Product product2 = Mockito.mock(Product.class);
        given(product2.getId()).willReturn(101L);
        given(product2.getName()).willReturn("테스트 상품2");
        given(product2.getPrice()).willReturn(20000);
        given(product2.getSellerId()).willReturn(5L);

        given(productSupport.findAllById(List.of(100L, 101L))).willReturn(List.of(product1, product2));

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getWishlistAllById(List.of(WISHLIST_ID))).willReturn(List.of(wishlist));

        // when
        Map<Long, WishlistItemSnapshot> result = wishlistItemService.getSnapshotList(wishlistItemIds);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1L).originalWishlistItemId()).isEqualTo(1L);
        assertThat(result.get(1L).productId()).isEqualTo(100L);
        assertThat(result.get(1L).productName()).isEqualTo("테스트 상품1");
        assertThat(result.get(1L).productPrice()).isEqualTo(10000);
        assertThat(result.get(1L).sellerId()).isEqualTo(5L);
        assertThat(result.get(1L).wishlistOwnerId()).isEqualTo(MEMBER_ID);
        assertThat(result.get(2L).originalWishlistItemId()).isEqualTo(2L);
        assertThat(result.get(2L).productName()).isEqualTo("테스트 상품2");
    }

    @Test
    @DisplayName("존재하지 않는 위시리스트 아이템 스냅샷 조회 시 예외 발생")
    void getSnapshot_List_NotFound() {
        // given
        List<Long> wishlistItemIds = List.of(999L);
        given(wishlistSupport.getWishlistItemListById(wishlistItemIds))
                .willThrow(new WishlistItemNotFoundException());

        // when & then
        assertThatThrownBy(() -> wishlistItemService.getSnapshotList(wishlistItemIds))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }

    @Test
    @DisplayName("스냅샷 조회 실패 - 상품 판매 상태 아님")
    void getSnapshot_List_ProductNotActive() {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L, 3L);

        WishlistItem wishlistItem1 = WishlistItem.builder().id(1L).wishlistId(WISHLIST_ID).productId(100L).build();
        WishlistItem wishlistItem2 = WishlistItem.builder().id(2L).wishlistId(WISHLIST_ID).productId(101L).build();
        WishlistItem wishlistItem3 = WishlistItem.builder().id(3L).wishlistId(WISHLIST_ID).productId(102L).build();
        given(wishlistSupport.getWishlistItemListById(wishlistItemIds))
                .willReturn(List.of(wishlistItem1, wishlistItem2, wishlistItem3));

        List<Product> products = List.of(
                Mockito.mock(Product.class),
                Mockito.mock(Product.class),
                Mockito.mock(Product.class)
        );
        given(productSupport.findAllById(List.of(100L, 101L, 102L))).willReturn(products);
        Mockito.doThrow(new ProductNotActiveException(101L))
                .when(productSupport).validatePurchasable(products);

        // when & then
        assertThatThrownBy(() -> wishlistItemService.getSnapshotList(wishlistItemIds))
                .isInstanceOf(ProductNotActiveException.class);
    }

    @Test
    @DisplayName("스냅샷 조회 실패 - 상품 재고 없음")
    void getSnapshot_List_ProductOutOfStock() {
        // given
        List<Long> wishlistItemIds = List.of(1L, 2L, 3L);

        WishlistItem wishlistItem1 = WishlistItem.builder().id(1L).wishlistId(WISHLIST_ID).productId(100L).build();
        WishlistItem wishlistItem2 = WishlistItem.builder().id(2L).wishlistId(WISHLIST_ID).productId(101L).build();
        WishlistItem wishlistItem3 = WishlistItem.builder().id(3L).wishlistId(WISHLIST_ID).productId(102L).build();
        given(wishlistSupport.getWishlistItemListById(wishlistItemIds))
                .willReturn(List.of(wishlistItem1, wishlistItem2, wishlistItem3));

        List<Product> products = List.of(
                Mockito.mock(Product.class),
                Mockito.mock(Product.class),
                Mockito.mock(Product.class)
        );
        given(productSupport.findAllById(List.of(100L, 101L, 102L))).willReturn(products);
        Mockito.doThrow(new ProductOutOfStockException(101L))
                .when(productSupport).validatePurchasable(products);

        // when & then
        assertThatThrownBy(() -> wishlistItemService.getSnapshotList(wishlistItemIds))
                .isInstanceOf(ProductOutOfStockException.class);
    }

    @Test
    @DisplayName("스냅샷 조회 시 요청 순서가 보장된다")
    void getSnapshot_List_OrderPreserved() {
        // given - 요청 순서: [3, 1, 2]
        List<Long> wishlistItemIds = List.of(3L, 1L, 2L);

        WishlistItem wishlistItem1 = WishlistItem.builder().id(1L).wishlistId(WISHLIST_ID).productId(100L).build();
        WishlistItem wishlistItem2 = WishlistItem.builder().id(2L).wishlistId(WISHLIST_ID).productId(101L).build();
        WishlistItem wishlistItem3 = WishlistItem.builder().id(3L).wishlistId(WISHLIST_ID).productId(102L).build();

        // DB 반환 순서: [1, 2, 3] (요청 순서와 다름)
        given(wishlistSupport.getWishlistItemListById(wishlistItemIds))
                .willReturn(List.of(wishlistItem1, wishlistItem2, wishlistItem3));

        Product product1 = Mockito.mock(Product.class);
        given(product1.getId()).willReturn(100L);
        given(product1.getName()).willReturn("상품1");
        given(product1.getPrice()).willReturn(10000);
        given(product1.getSellerId()).willReturn(5L);

        Product product2 = Mockito.mock(Product.class);
        given(product2.getId()).willReturn(101L);
        given(product2.getName()).willReturn("상품2");
        given(product2.getPrice()).willReturn(20000);
        given(product2.getSellerId()).willReturn(5L);

        Product product3 = Mockito.mock(Product.class);
        given(product3.getId()).willReturn(102L);
        given(product3.getName()).willReturn("상품3");
        given(product3.getPrice()).willReturn(30000);
        given(product3.getSellerId()).willReturn(5L);

        given(productSupport.findAllById(List.of(100L, 101L, 102L))).willReturn(List.of(product1, product2, product3));

        Wishlist wishlist = Wishlist.builder()
                .id(WISHLIST_ID)
                .memberId(MEMBER_ID)
                .visibility(Visibility.PUBLIC)
                .build();
        given(wishlistSupport.getWishlistAllById(List.of(WISHLIST_ID))).willReturn(List.of(wishlist));

        // when
        Map<Long, WishlistItemSnapshot> result = wishlistItemService.getSnapshotList(wishlistItemIds);

        // then - key로 직접 접근하여 올바른 스냅샷이 매핑되었는지 검증
        assertThat(result).hasSize(3);
        assertThat(result.get(3L).originalWishlistItemId()).isEqualTo(3L);
        assertThat(result.get(3L).productName()).isEqualTo("상품3");
        assertThat(result.get(1L).originalWishlistItemId()).isEqualTo(1L);
        assertThat(result.get(1L).productName()).isEqualTo("상품1");
        assertThat(result.get(2L).originalWishlistItemId()).isEqualTo(2L);
        assertThat(result.get(2L).productName()).isEqualTo("상품2");
    }
}
