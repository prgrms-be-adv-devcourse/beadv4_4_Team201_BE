package app.giftify.cart.core.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

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
    private final Map<Long, CartItem> items; // 복합 키 사용

    private Cart(Long id, Long memberId, Map<Long, CartItem> items) {
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
    public static Cart reconstruct(Long id, Long memberId, Map<Long, CartItem> items) {
        return new Cart(id, memberId, items);
    }

    /*
    장바구니에 상품 추가
     */
    public CartItemAddResult addItem(Long wishlistItemId, Money amount) {
        // 금액 검증은 CartItem이 함
        if (items.containsKey(wishlistItemId)) {
            items.get(wishlistItemId).updateAmount(amount);
            return CartItemAddResult.UPDATED;
        } else {
            items.put(wishlistItemId, CartItem.create(this.getId(), wishlistItemId, amount));
            return CartItemAddResult.ADDED;
        }
    }

    /*
    장바구니에서 상품 삭제
     */
    public void removeItems(List<Long> wishlistItemIds) {
        for (Long wishlistItemId : wishlistItemIds) {
            // 없으면 무시하고 다음 아이템으로 진행
            items.remove(wishlistItemId);
        }
    }

    public void clearItems() {
        items.clear();
    }

    public Long getMemberId() { return memberId; }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public int getItemCount() { return items.size(); }

}
