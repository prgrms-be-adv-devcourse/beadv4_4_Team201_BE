package app.giftify.cart.adapter.outbound.mapper;

import app.giftify.cart.adapter.outbound.jpa.JpaCartItem;
import app.giftify.cart.core.domain.CartItem;
import app.giftify.support.common.money.Money;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public JpaCartItem toJpaEntity(CartItem cartItem) {
        return JpaCartItem.from(
                cartItem.getId(),
                cartItem.getWishlistItemId(),
                cartItem.getAmount().amount()
        );
    }

    public CartItem toDomain(JpaCartItem jpaCartItem) {
        return CartItem.reconstruct(
                jpaCartItem.getId(),
                jpaCartItem.getCartId(),
                jpaCartItem.getWishlistItemId(),
                Money.of(jpaCartItem.getAmount())
        );
    }
}