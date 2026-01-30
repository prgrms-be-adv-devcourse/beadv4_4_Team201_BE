package app.giftify.cart.application.inbound;

import app.giftify.cart.application.outbound.CartRepository;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements AddCartItemUseCase, CartCreateUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Override
    public Cart createCart(Long memberId) {
        return cartRepository.save(Cart.create(memberId));
    }

    @Override
    public Cart addItem(Long memberId, AddCartItemCommand command) {
        Cart cart = cartRepository.findByMemberId(memberId)
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

        return cartRepository.save(cart);
    }

    private void validateCartItem(TargetType targetType, Long targetId) {
        switch (targetType) {
            case PRODUCT -> validateDirectPurchase(targetId);
            case FUNDING -> validateFundingPurchase(targetId);
            default -> throw new CartException(CartErrorCode.INVALID_TARGET_TYPE);
        };
    }

    // 일반 구매 검증
    private void validateDirectPurchase(Long productId) {
        Product product = productRepository.findById(productId)
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
}
