package app.giftify.cart.application.inbound;

import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.CartItemAddResult;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.cart.readmodel.MemberView;
import app.giftify.cart.readmodel.MemberViewRepository;
import app.giftify.shared.domain.port.FundingQueryPort;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    @Mock
    private MemberViewRepository memberRepository;

    @Mock
    private FundingQueryPort fundingQueryPort;

    private final Long memberId = 1L;
    private final Long cartId = 1L;

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(PENDING) + 상품(ACTIVE) + 재고 있음")
    void addItem_Funding_Success() {
        // given
        Long wishlistId = 3L;
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
        given(wishlistItem.getWishlistId()).willReturn(wishlistId);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(10); // 재고 있음
        given(product.getPrice()).willReturn(50000);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        given(fundingQueryPort.findFundingInfoByWishlistItemId(wishlistItemId)).willReturn(Optional.empty());


        AddCartItemCommand command = new AddCartItemCommand(wishlistId, wishlistItemId, amount);

        // when
        CartItemAddResult result = cartService.upsertCartItem(memberId, command);

        // then
        assertThat(result).isEqualTo(CartItemAddResult.ADDED);
    }

    @Test
    @DisplayName("펀딩 상품 추가 성공: 위시리스트 아이템(IN_PROGRESS) + 상품(ACTIVE) + 재고 있음")
    void addItem_FundingInProgress_Success() {
        // given
        Long wishlistId = 3L;
        Long wishlistItemId = 101L; // 다른 ID 사용
        Long productId = 201L;
        Money amount = Money.of(10000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.IN_PROGRESS); // IN_PROGRESS 상태
        given(wishlistItem.getWishlistId()).willReturn(wishlistId);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        Product product = mock(Product.class);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(10); // 재고 있음
        given(product.getPrice()).willReturn(50000);
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        given(fundingQueryPort.findFundingInfoByWishlistItemId(wishlistItemId)).willReturn(Optional.empty());


        AddCartItemCommand command = new AddCartItemCommand(wishlistId, wishlistItemId, amount);

        // when
        CartItemAddResult result = cartService.upsertCartItem(memberId, command);

        // then
        assertThat(result).isEqualTo(CartItemAddResult.ADDED);
    }

    @Test
    @DisplayName("펀딩 상품 추가 실패: 상품이 ACTIVE가 아님")
    void addItem_Funding_Fail_ProductInactive() {
        // given
        Long wishlistId = 3L;
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
        // given(product.getStock()).willReturn(10); // 상태가 먼저 체크되므로 재고는 상관없을 수 있음
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(wishlistId, wishlistItemId, amount);

        // when & then
        assertThatThrownBy(() -> cartService.upsertCartItem(memberId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("펀딩 상품 추가 실패: 재고 없음")
    void addItem_Funding_Fail_OutOfStock() {
        // given
        Long wishlistId = 3L;
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
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(0); // 실패 조건: 재고 0
        given(productRepositoryPort.findById(productId)).willReturn(Optional.of(product));

        AddCartItemCommand command = new AddCartItemCommand(wishlistId, wishlistItemId,amount);

        // when & then
        assertThatThrownBy(() -> cartService.upsertCartItem(memberId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("펀딩 상품 추가 실패: WishlistItem의 상태가 COMPLETED임")
    void addItem_Product_Fail_InvalidTargetType() {
        // given
        Long wishlistId = 3L;
        Long wishlistItemId = 100L;
        Long productId = 300L;
        Money amount = Money.of(50000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        // 실패 조건: WishlistItem의 상태가 COMPLETED
        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.COMPLETED);
        given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

        AddCartItemCommand command = new AddCartItemCommand(wishlistId, wishlistItemId,amount);

        // when & then
        assertThatThrownBy(() -> cartService.upsertCartItem(memberId, command))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 조회 성공")
    void getCart_Success() {
        // given
        Long productId = 300L;
        Money amount = Money.of(50000);

        Cart cart = Cart.create(memberId);

        given(cartRepository.findById(cartId)).willReturn(Optional.of(cart));

        // when
        CartResponse response = cartService.getCart(cartId, memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.items()).isEmpty(); // 아이템이 없으므로 비어있어야 함
    }

    @Test
    @DisplayName("장바구니 조회 시 아이템에 receiverId 포함")
    void getMyCart_ContainsReceiverId() {
        // given
        Long wishlistItemId = 100L;
        Long wishlistId = 10L;
        Long receiverId = 99L;
        Long productId = 200L;

        Cart cart = Cart.create(memberId);
        cart.addItem(wishlistItemId, Money.of(10000));
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));

        WishlistItem wishlistItem = mock(WishlistItem.class);
        given(wishlistItem.getId()).willReturn(wishlistItemId);
        given(wishlistItem.getWishlistId()).willReturn(wishlistId);
        given(wishlistItem.getProductId()).willReturn(productId);
        given(wishlistItem.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItemRepositoryPort.findAllById(List.of(wishlistItemId)))
                .willReturn(List.of(wishlistItem));

        Wishlist wishlist = mock(Wishlist.class);
        given(wishlist.getId()).willReturn(wishlistId);
        given(wishlist.getMemberId()).willReturn(receiverId);
        given(wishlistRepositoryPort.findAllById(List.of(wishlistId)))
                .willReturn(List.of(wishlist));

        MemberView receiver = mock(MemberView.class);
        given(receiver.getId()).willReturn(receiverId);
        given(receiver.getNickname()).willReturn("test-receiver");
        given(memberRepository.findAllById(anySet())).willReturn(List.of(receiver));

        Product product = mock(Product.class);
        given(product.getId()).willReturn(productId);
        given(product.getName()).willReturn("테스트상품");
        given(product.getPrice()).willReturn(50000);
        given(product.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product.getStock()).willReturn(10);
        given(productRepositoryPort.findAllById(List.of(productId)))
                .willReturn(List.of(product));

        given(fundingQueryPort.findFundingInfoByWishlistItemIds(anyList())).willReturn(Collections.emptyMap());


        // when
        CartResponse response = cartService.getMyCart(memberId);

        // then
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).receiverId()).isEqualTo(receiverId);
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
    void removeItems_Success() {
        // given
        Cart cart = Cart.create(memberId);
        cart.addItem(100L, Money.of(10000));
        cart.addItem(10L, Money.of(20000));
        cart.addItem(20L, Money.of(5000));
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        cartService.removeItems(memberId, List.of(100L, 10L));

        // then
        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getItems().stream()
                .map(CartItem::getAmount)
                .reduce(Money.zero(), Money::plus))
                .isEqualTo(Money.of(5000));
        then(cartRepository).should().save(cart);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 실패: 장바구니 없음")
    void removeItems_Fail_CartNotFound() {
        // given
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartService.removeItems(memberId,List.of(100L)))
                .isInstanceOf(CartException.class);
    }

    @Test
    @DisplayName("장바구니 비우기 성공")
    void clearCart_Success() {
        // given
        Cart cart = Cart.create(memberId);
        cart.addItem(100L, Money.of(10000));
        cart.addItem(200L, Money.of(20000));
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

    @Test
    @DisplayName("카트 생성 성공: 기존 카트가 없을 경우")
    void createCart_Success_NoExistingCart() {
        // given
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> {
            Cart newCart = invocation.getArgument(0);
            // Simulate saving by returning the same object
            return newCart;
        });

        // when
        Cart result = cartService.createCart(memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        then(cartRepository).should().save(any(Cart.class));
    }


    @Test
    @DisplayName("카트 생성 멱등성: 이미 존재하면 기존 카트를 반환한다")
    void createCart_Idempotent_ReturnsExisting() {
        // given
        Cart existingCart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(existingCart));

        // when
        Cart result = cartService.createCart(memberId);

        // then
        assertThat(result).isSameAs(existingCart);
        then(cartRepository).should(never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("여러 상품 장바구니 추가 성공")
    void upsertCart_Items_Success() {
        // given
        Long wishlistId = 3L;
        Long wishlistItemId1 = 100L;
        Long productId1 = 200L;
        Money amount1 = Money.of(10000);

        Long wishlistItemId2 = 101L;
        Long productId2 = 201L;
        Money amount2 = Money.of(20000);

        // Cart Mocking
        Cart cart = Cart.create(memberId);
        given(cartRepository.findByMemberId(memberId)).willReturn(Optional.of(cart));
        given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Item 1 Mocking
        WishlistItem wishlistItem1 = mock(WishlistItem.class);
        given(wishlistItem1.getProductId()).willReturn(productId1);
        given(wishlistItem1.getWishlistItemStatus()).willReturn(WishlistItemStatus.PENDING);
        given(wishlistItem1.getWishlistId()).willReturn(wishlistId);
        given(wishlistItemRepositoryPort.findById(wishlistItemId1)).willReturn(Optional.of(wishlistItem1));

        Product product1 = mock(Product.class);
        given(product1.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product1.getStock()).willReturn(10);
        given(product1.getPrice()).willReturn(50000);
        given(productRepositoryPort.findById(productId1)).willReturn(Optional.of(product1));

        // Item 2 Mocking
        WishlistItem wishlistItem2 = mock(WishlistItem.class);
        given(wishlistItem2.getProductId()).willReturn(productId2);
        given(wishlistItem2.getWishlistItemStatus()).willReturn(WishlistItemStatus.IN_PROGRESS);
        given(wishlistItem2.getWishlistId()).willReturn(wishlistId);
        given(wishlistItemRepositoryPort.findById(wishlistItemId2)).willReturn(Optional.of(wishlistItem2));

        Product product2 = mock(Product.class);
        given(product2.getStatus()).willReturn(ProductStatus.ACTIVE);
        given(product2.getStock()).willReturn(5);
        given(product2.getPrice()).willReturn(50000);
        given(productRepositoryPort.findById(productId2)).willReturn(Optional.of(product2));

        given(fundingQueryPort.findFundingInfoByWishlistItemId(anyLong())).willReturn(Optional.empty());


        List<AddCartItemCommand> commands = List.of(
                new AddCartItemCommand(wishlistId, wishlistItemId1, amount1),
                new AddCartItemCommand(wishlistId, wishlistItemId2, amount2)
        );

        // when
        cartService.upsertCartItems(memberId, commands);

        // then
        assertThat(cart.getItemCount()).isEqualTo(2);
        assertThat(cart.getItems().stream()
                .map(CartItem::getAmount)
                .reduce(Money.zero(), Money::plus))
                .isEqualTo(Money.of(30000));
        then(cartRepository).should().save(cart);
    }
}
