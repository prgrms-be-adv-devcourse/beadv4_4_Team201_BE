package app.giftify.wishlist.core.domain.exception;

public class ProductNotOnSaleException extends WishlistDomainException {
    public ProductNotOnSaleException(Long productId) {
        super(WishlistErrorCode.INVALID_WISHLIST_ITEM_STAUTS, "현재 판매 중이지 않은 상품입니다. (productId: " + productId + ")");
    }
}
