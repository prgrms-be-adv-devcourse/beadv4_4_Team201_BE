package app.giftify.cart.core.domain;

import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 장바구니란 무엇인가,
 * 순수 비즈니스 (엔티티, 값 객체, 도메인 규칙, 도메인 인터페이스)
 * 절대 금지 : JAP, Controller, 다른 모듈 직접 참조
 */
public class Cart extends BaseDomainModel {
    private final Long memberId;
    private final Map<CartItemKey, CartItem> items; // 복합 키 사용

    private Cart(Long id, Long memberId, Map<CartItemKey, CartItem> items) {
        super(id);
        this.memberId = memberId;
        this.items = items != null ? new HashMap<>(items) : new HashMap<>(); // 방어적 복사
    }

    public static Cart create(Long memberId) {
        return new Cart(null, memberId, new HashMap<>());
    }

    /*
     DB에서 조회한 데이터로 재구성
     */
    public static Cart reconstruct(Long id, Long memberId, Map<CartItemKey, CartItem> items) {
        return new Cart(id, memberId, items);
    }

    /*
    장바구니에 상품 추가
     */
    public void addItem(TargetType targetType, Long targetId, Money amount, WishlistItemStatus wishlistItemStatus) {
        // 금액 검증은 CartItem이 함
        CartItemKey key = new CartItemKey(targetType, targetId);

        if (items.containsKey(key)) {
            items.get(key).changeAmount(amount);
        } else {
            items.put(key, CartItem.create(this.getId(), targetType, targetId, amount, wishlistItemStatus));
        }
    }

    /*
    장바구니에서 상품 삭제
     */
    public void removeItem(TargetType targetType, Long targetId) {
        CartItemKey key = new CartItemKey(targetType, targetId);

        if (!items.containsKey(key)) {
            throw new CartException(CartErrorCode.CARTITEM_NOT_FOUND);
        }

        items.remove(key);
    }

    public Long getMemberId() { return memberId; }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public int getItemCount() { return items.size(); }

}