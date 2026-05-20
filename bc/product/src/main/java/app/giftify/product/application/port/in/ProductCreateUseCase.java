package app.giftify.product.application.port.in;

public interface ProductCreateUseCase {
    ProductResult createProduct(Long sellerId, ProductCreateCommand command);
}
