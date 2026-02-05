package app.giftify.cart.application.inbound;

import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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

    private Long memberId = 1L;
    private Long cartId = 1L;

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(PENDING) + 상품(ACTIVE)")
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
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING_PENDING, wishlistItemId),
                amount
        );

        // when
        Cart resultCart = cartService.addItem(cartId, command);

        // then
        assertThat(resultCart.getItems()).hasSize(1);
        assertThat(resultCart.getItems().get(0).getTargetId()).isEqualTo(wishlistItemId);
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
    @DisplayName("일반 상품 추가 성공: 상품(ACTIVE)")
    void addItem_Product_Success() {
        // given
        Long productId = 300L;
        Money amount = Money.of(50000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.GENERAL_PRODUCT, productId),
                amount
        );

        // when
        Cart resultCart = cartService.addItem(cartId, command);

        // then
        assertThat(resultCart.getItems()).hasSize(1);
        assertThat(resultCart.getItems().get(0).getTargetId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("일반 상품 추가 실패: 상품(INACTIVE)")
    void addItem_Product_Fail_Inactive() {
        // given
        Long productId = 300L;
        Money amount = Money.of(50000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.INACTIVE);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

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
        cart.addItem(TargetType.GENERAL_PRODUCT, productId, amount);

        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        Product product = mock(Product.class);
        given(product.getId()).willReturn(productId);
        given(product.getName()).willReturn("Test Product");
        given(product.getPrice()).willReturn(amount.amount().intValue());
        given(productRepositoryPort.findAllById(anyList())).willReturn(List.of(product));

        // when
        CartResponse response = cartService.getCart(cartId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("Test Product");
        assertThat(response.totalAmount()).isEqualTo(amount.amount().longValue());
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
}
