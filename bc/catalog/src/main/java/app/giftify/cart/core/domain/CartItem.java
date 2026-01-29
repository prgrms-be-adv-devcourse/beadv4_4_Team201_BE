package app.giftify.cart.core.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

/**
 * 설계에선 수량필드가 있었는데, 이는 일반결제일때만 필요하고 펀딩카트에선 필요 없으므로 우선 지움
 */
public class CartItem extends BaseDomainModel {
    private final Long cartId;
    private TargetType targetType;
    private Long targetId;    // String ?
    private Money amount;
    private final WishlistItemStatus wishlistItemStatus;
    private static final Money MIN_CONTRIBUTION = Money.of(1000);

    private CartItem(Long id, Long cartId, TargetType targetType, Long targetId, Money amount, WishlistItemStatus wishlistItemStatus) {
        super(id);
        this.cartId = cartId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.amount = amount;
        this.wishlistItemStatus = wishlistItemStatus;
        validate(amount);
    }

    /*
    장바구니에 담을 새로운 항목 객체 생성
     */
    public static CartItem create(Long cartId, TargetType targetType, Long targetId, Money amount, WishlistItemStatus wishlistItemStatus) {
        // 생성 시점에는 ID가 없으므로 null 전달
        return new CartItem(null, cartId, targetType, targetId, amount, wishlistItemStatus);
    }

    public void changeAmount(Money newAmount) {
        validate(newAmount);
        this.amount = newAmount;
    }

    public void validate(Money amount) {
        if (amount == null || amount.isLessThan(MIN_CONTRIBUTION)) {
            throw new IllegalArgumentException("금액은 1000원 이상이어야 합니다.");
        }
    }

    // DB에서 조회한 데이터로 재구성할 때 사용
    public static CartItem reconstruct(Long id, Long cartId, TargetType targetType, Long targetId, Money amount, WishlistItemStatus wishlistItemStatus) {
        if (id == null) {
            throw new IllegalArgumentException("재구성 시에는 ID가 필수입니다");
        }
        return new CartItem(id, cartId, targetType, targetId, amount, wishlistItemStatus);
    }

    public TargetType getTargetType() { return targetType; }

    public Long getTargetId() { return targetId; }

    public Money getAmount() { return amount; }

    public WishlistItemStatus getWishlistItemStatus() {return wishlistItemStatus;}

    @Override
    public String toString() {
        return "CartItem{" +
                "cartId=" + cartId +
                ", targetType=" + targetType +
                ", targetId='" + targetId + '\'' +
                ", amount=" + amount +
                ", wishlistItemStatus=" + wishlistItemStatus +
                '}';
    }
}
