package app.giftify.cart.adapter.outbound.mapper;

import app.giftify.cart.adapter.outbound.jpa.JpaCart;
import app.giftify.cart.adapter.outbound.jpa.JpaCartItem;
import app.giftify.cart.core.domain.Cart;
import app.giftify.cart.core.domain.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final CartItemMapper cartItemMapper;
/**
 * 도메인 -> 엔티티 (저장할 때)
 */
public JpaCart toJpaEntity(Cart cart) {
    // JpaCart 생성 (items 없이)
    JpaCart jpaCart = JpaCart.from(cart.getId(), cart.getMemberId());

    // JpaCartItem 생성 및 연관관계 설정
    cart.getItems().forEach(item -> {
        JpaCartItem jpaCartItem = cartItemMapper.toJpaEntity(item);
                jpaCart.addItem(jpaCartItem);
            });
    return jpaCart;
}

    /**
     * 엔티티 -> 도메인 (조회할 때)
     */
    public Cart toDomain(JpaCart jpaCart) {
        Map<Long, CartItem> items = jpaCart.getItems().stream()
                .collect(Collectors.toMap(
                        itemEntity -> itemEntity.getWishlistItemId(),
                        cartItemMapper::toDomain  // ✅ Mapper 재사용
                ));

//        도메인의 reconstruct 메서드를 사용하여 상태를 복구
        return Cart.reconstruct(jpaCart.getId(), jpaCart.getMemberId(), items);
    }
}

