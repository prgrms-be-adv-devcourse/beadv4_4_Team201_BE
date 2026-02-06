package app.giftify.product.domain.exception;

public class ProductOutOfStockException extends ProductException {
    public ProductOutOfStockException(Long id) {
        super(ProductErrorCode.PRODUCT_OUT_OF_STOCK, "상품 재고가 부족합니다. (productId: " + id + ")");
    }
}
