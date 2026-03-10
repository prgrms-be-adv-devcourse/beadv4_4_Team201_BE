package app.giftify.product.application.port.in;

public interface ProductDeleteUseCase {
    void deleteProduct(Long productId, Long sellerId);
}
