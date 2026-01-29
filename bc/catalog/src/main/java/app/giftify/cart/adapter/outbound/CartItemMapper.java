package app.giftify.cart.adapter.outbound;

import app.giftify.cart.adapter.outbound.jpa.JpaCartItem;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public JpaCartItem toJpaEntity(CartItem cartItem) {
        return JpaCartItem.from(
                cartItem.getId(),
                cartItem.getTargetType(),
                cartItem.getTargetId(),
                cartItem.getAmount().amount(),
                cartItem.getWishlistItemStatus()
        );
    }

    public CartItem toDomain(JpaCartItem jpaCartItem) {
        return CartItem.reconstruct(
                jpaCartItem.getId(),
                jpaCartItem.getCartId(),
                jpaCartItem.getTargetType(),
                jpaCartItem.getTargetId(),
                Money.of(jpaCartItem.getAmount()),
                jpaCartItem.getWishlistItemStatus()
        );
    }
}