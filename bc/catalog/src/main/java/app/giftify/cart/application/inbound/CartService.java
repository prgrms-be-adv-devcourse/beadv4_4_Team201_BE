package app.giftify.cart.application.inbound;

import app.giftify.cart.application.outbound.CartRepository;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.product.adapter.outbound.ProductRepository;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements AddCartItemUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;


    @Override
    public Cart addItem(Long memberId, AddCartItemCommand command) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(()-> new CartException(CartErrorCode.CART_NOT_FOUND));

        // 상품 상태는 ACTIVE 상태 && 위시리스트아이템 상태는 PENDING, IN_PROGRESS 상태만 담을 수 있음
        Product product = productRepository.findById(command.cartItemKey().targetId())
                .orElseThrow(() -> new CartException(CartErrorCode.INVALID_ITEM_STATUS));
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new CartException(CartErrorCode.CANNOT_ADD_ITEM);
        }

        WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(command.cartItemKey().targetId())
                .orElseThrow(() -> new CartException(CartErrorCode.INVALID_ITEM_STATUS));
        if (wishlistItem.getWishlistItemStatus() != WishlistItemStatus.PENDING && wishlistItem.getWishlistItemStatus() != WishlistItemStatus.IN_PROGRESS) {
            throw new CartException(CartErrorCode.CANNOT_ADD_ITEM);
        }

        /**
         * 아이템 추가
         * 이미 담긴 상품이면 가격 변경 / 카트에 없는 상품이면 추가 -> 도메인에 로직 있음
         */
        cart.addItem(
                command.cartItemKey().targetType(),
                command.cartItemKey().targetId(),
                command.amount(),
                wishlistItem.getWishlistItemStatus()
        );

        return cartRepository.save(cart);
    }
}
