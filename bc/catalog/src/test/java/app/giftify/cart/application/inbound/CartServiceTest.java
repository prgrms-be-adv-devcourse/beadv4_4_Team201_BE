package app.giftify.cart.application.inbound;

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

import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepositoryPort cartRepository;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    private final Long memberId = 1L;
    private final Long cartId = 1L;

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(PENDING) + 상품(ACTIVE) + 재고 있음")
    void addItem_Funding_Success() {
        // given
        Long wishlistItemId = 100L;
        Long productId = 200L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(10); // 재고 있음
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING_PENDING, wishlistItemId),
                amount
        );

        // when
        CartItemAddResult result = cartService.addItem(cartId, command);

        // then
        assertThat(result).isEqualTo(CartItemAddResult.ADDED);
    }

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(IN_PROGRESS) + 상품(ACTIVE) + 재고 있음")
    void addItem_FundingInProgress_Success() {
        // given
        Long wishlistItemId = 101L; // 다른 ID 사용
        Long productId = 201L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.IN_PROGRESS); // IN_PROGRESS 상태
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(10); // 재고 있음
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING, wishlistItemId), // FUNDING 타입
                amount
        );

        // when
        CartItemAddResult result = cartService.addItem(cartId, command);

        // then
        assertThat(result).isEqualTo(CartItemAddResult.ADDED);
    }

    @Test
    @DisplayName("펀딩 상품 추가 실패: 상품이 ACTIVE가 아님")
    void addItem_Funding_Fail_ProductInactive() {
        // given
        Long wishlistItemId = 100L;
        Long productId = 200L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.INACTIVE); // 실패 조건
        // given(product.getStock()).willReturn(10); // 상태가 먼저 체크되므로 재고는 상관없을 수 있음
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING_PENDING, wishlistItemId),
                amount
        );

        // when & then
        assertThatThrownBy(() -> cartService.addItem(cartId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("펀딩 상품 추가 실패: 재고 없음")
    void addItem_Funding_Fail_OutOfStock() {
        // given
        Long wishlistItemId = 100L;
        Long productId = 200L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(0); // 실패 조건: 재고 0
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING_PENDING, wishlistItemId),
                amount
        );

        // when & then
        assertThatThrownBy(() -> cartService.addItem(cartId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("일반 상품 추가 실패: TargetType이 FUNDING_PENDING이 아님")
    void addItem_Product_Fail_InvalidTargetType() {
        // given
        Long productId = 300L;
        Money amount = Money.of(50000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.GENERAL_PRODUCT, productId),
                amount
        );

        // when & then
        assertThatThrownBy(() -> cartService.addItem(cartId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_Success() {
        // given
        Long productId = 300L;
        Money amount = Money.of(50000);

        Cart cart = Cart.create(memberId);
        // cart.addItem(TargetType.GENERAL_PRODUCT, productId, amount); // GENERAL_PRODUCT는 현재 addItem에서 막힘

        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.getCart(cartId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.items()).isEmpty(); // 아이템이 없으므로 비어있어야 함
    }

    @Test
    @DisplayName("장바구니 조회 실패: 장바구니 없음")
    void getCart_Fail_NotFound() {
        // given
        given(cartRepository.findById(cartId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.getCart(cartId, memberId))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 조회 실패: 권한 없음")
    void getCart_Fail_Forbidden() {
        // given
        Long otherMemberId = 2L;
        Cart cart = Cart.create(otherMemberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        // when & then
        assertThatThrownBy(() -> cartService.getCart(cartId, memberId))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("내 장바구니 조회 성공")
    void getMyCart_Success() {
        // given
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.getMyCart(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.items()).isEmpty();
    }

    @Test
    @DisplayName("내 장바구니 조회 실패: 장바구니 없음")
    void getMyCart_Fail_NotFound() {
        // given
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.getMyCart(memberId))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void removeItem_Success() {
        // given
        Cart cart = Cart.create(memberId);
        cart.addItem(TargetType.FUNDING_PENDING, 100L, Money.of(10000));
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        cartService.removeItem(memberId, TargetType.FUNDING_PENDING, 100L);

        // then
        assertThat(cart.getItemCount()).isZero();
        then(cartRepository).should().save(cart);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패: 장바구니 없음")
    void removeItem_Fail_CartNotFound() {
        // given
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.removeItem(memberId, TargetType.FUNDING_PENDING, 100L))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패: 아이템 없음")
    void removeItem_Fail_ItemNotFound() {
        // given
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // when & then
        assertThatThrownBy(() -> cartService.removeItem(memberId, TargetType.FUNDING_PENDING, 999L))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 비우기 성공")
    void clearCart_Success() {
        // given
        Cart cart = Cart.create(memberId);
        cart.addItem(TargetType.FUNDING_PENDING, 100L, Money.of(10000));
        cart.addItem(TargetType.FUNDING, 200L, Money.of(20000));
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        cartService.clearCart(memberId);

        // then
        assertThat(cart.getItemCount()).isZero();
        then(cartRepository).should().save(cart);
    }

    @Test
    @DisplayName("장바구니 비우기 실패: 장바구니 없음")
    void clearCart_Fail_CartNotFound() {
        // given
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.clearCart(memberId))
                .isInstanceOf(CartException.class);
    }
}
