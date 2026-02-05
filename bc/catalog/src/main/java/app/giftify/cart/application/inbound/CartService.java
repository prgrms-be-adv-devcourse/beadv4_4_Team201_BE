package app.giftify.cart.application.inbound;

import app.giftify.cart.adapter.inbound.CartResponse;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements AddCartItemUseCase, CartCreateUseCase, GetCartUseCase {
    private final CartRepositoryPort cartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Override
    public Cart createCart(Long memberId) {
        return cartRepositoryPort.save(Cart.create(memberId));
    }

    @Override
    public Cart addItem(Long cartId, AddCartItemCommand command) {
        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        TargetType targetType = command.cartItemKey().targetType();
        Long targetId = command.cartItemKey().targetId();

        // 타입별 검증
        validateCartItem(targetType, targetId);

        cart.addItem(
                targetType,
                targetId,
                command.amount()
        );

        return cartRepositoryPort.save(cart);
    }

    private void validateCartItem(TargetType targetType, Long targetId) {
        switch (targetType) {
            case GENERAL_PRODUCT -> validateDirectPurchase(targetId);
            case FUNDING_PENDING -> validateFundingPurchase(targetId);
            default -> throw new CartException(CartErrorCode.INVALID_TARGET_TYPE);
        }
        ;
    }

    // 일반 구매 검증
    private void validateDirectPurchase(Long productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new CartException(CartErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new CartException(CartErrorCode.INVALID_ITEM_STATUS);
        }
    }

    // 펀딩 구매 검증 (Product + WishlistItem 둘 다 검증)
    private void validateFundingPurchase(Long wishlistItemId) {
        WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(wishlistItemId)
                .orElseThrow(() -> new CartException(CartErrorCode.WISHLIST_ITEM_NOT_FOUND));

        // WishlistItem 상태 검증
        WishlistItemStatus status = wishlistItem.getWishlistItemStatus();
        if (status != WishlistItemStatus.PENDING && status != WishlistItemStatus.IN_PROGRESS) {
            throw new CartException(CartErrorCode.INVALID_ITEM_STATUS);
        }

        // 원본 Product 상태 검증
        validateDirectPurchase(wishlistItem.getProductId());
    }

    // 카트 조회(아이템 목록)
    @Override
    public CartResponse getCart(Long cartId, Long memberId) {
        Cart cart = cartRepositoryPort.findById(cartId)
                .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        if (!cart.getMemberId().equals(memberId)) { throw new CartException(CartErrorCode.FORBIDDEN); }

        // 장바구니 아이템들의 상품ID 목록 추출
        List<Long> productIds = cart.getItems().stream()
                .map(CartItem::getTargetId)
                .collect(Collectors.toList());

        // 상품 정보 조회 및 Map으로 변환
        Map<Long, Product> productMap = productRepositoryPort.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        return CartResponse.from(cart, productMap);
    }
}
