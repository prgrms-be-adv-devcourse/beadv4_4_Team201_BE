package app.giftify.cart.core.domain;

import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.support.common.base.BaseDomainModel;
import app.giftify.support.common.money.Money;

/**
 * 설계에선 수량필드가 있었는데, 이는 일반결제일때만 필요하고 펀딩카트에선 필요 없으므로 우선 지움
 */
public class CartItem extends BaseDomainModel {
    private final Long cartId;
    private final Long wishlistItemId;
    private Money amount;
    private static final Money MIN_CONTRIBUTION = Money.of(1000);

    private CartItem(Long id, Long cartId, Long wishlistItemId, Money amount) {
        super(id);
        this.cartId = cartId;
        this.wishlistItemId = wishlistItemId;
        this.amount = amount;
        validate(amount);
    }

    /*
    장바구니에 담을 새로운 항목 객체 생성
     */
    public static CartItem create(Long cartId, Long wishlistItemId, Money amount) {
        // 생성 시점에는 ID가 없으므로 null 전달
        return new CartItem(null, cartId, wishlistItemId, amount);
    }

    public void updateAmount(Money newAmount) {
        validate(newAmount);
        this.amount = newAmount;
    }

    public void validate(Money amount) {
        if (amount == null || amount.isLessThan(MIN_CONTRIBUTION)) {
            throw new CartException(CartErrorCode.INVALID_AMOUNT);
        }
    }

    // DB에서 조회한 데이터로 재구성할 때 사용
    public static CartItem reconstruct(Long id, Long cartId, Long wishlistItemId, Money amount) {
        if (id == null) {
            throw new CartException(CartErrorCode.CARTITEM_ID_REQUIRED);
        }
        return new CartItem(id, cartId, wishlistItemId, amount);
    }

    public Long getWishlistItemId() { return wishlistItemId; }

    public Money getAmount() { return amount; }

    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + cartId +
                ", wishlistItemId='" + wishlistItemId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
