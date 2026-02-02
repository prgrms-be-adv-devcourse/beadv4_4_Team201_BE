package app.giftify.product.application.port.in;

public interface ProductGetUseCase {
    /**
     * 상품 단건 조회
     */
    ProductResult getProduct(Long productId);
}
