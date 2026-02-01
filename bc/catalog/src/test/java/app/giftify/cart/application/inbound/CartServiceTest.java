package app.giftify.cart.application.inbound;

import app.giftify.cart.application.outbound.CartRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    private Long memberId = 1L;

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(PENDING) + 상품(ACTIVE)")
    void addItem_Funding_Success() {
        // given
        Long wishlistItemId = 100L;
        Long productId = 200L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING, wishlistItemId),
                amount
        );

        // when
        Cart resultCart = cartService.addItem(memberId, command);

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
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.INACTIVE); // 실패 조건
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.FUNDING, wishlistItemId),
                amount
        );

        // when & then
        assertThatThrownBy(() -> cartService.addItem(memberId, command))
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
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.PRODUCT, productId),
                amount
        );

        // when
        Cart resultCart = cartService.addItem(memberId, command);

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
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.INACTIVE);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(
                new CartItemKey(TargetType.PRODUCT, productId),
                amount
        );

        // when & then
        assertThatThrownBy(() -> cartService.addItem(memberId, command))
                .isInstanceOf(CartException.class);
    }
}
