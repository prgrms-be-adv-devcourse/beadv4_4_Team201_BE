package app.giftify.product.domain.exception;

public class ProductNotActiveException extends ProductException {
    public ProductNotActiveException(Long id) {
        super(ProductErrorCode.PRODUCT_NOT_ACTIVE, "판매 중인 상품이 아닙니다. (productId: " + id + ")");
    }
}
