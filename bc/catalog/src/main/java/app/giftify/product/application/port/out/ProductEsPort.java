package app.giftify.product.application.port.out;

import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.domain.Product;
import app.giftify.shared.api.paging.PageResponse;

public interface ProductEsPort {
    void save(Product product);

    int syncAll();

    PageResponse<ProductResult> searchProducts(ProductEsSearchCommand command);

    void deleteById(Long productId);

    void updateSellerNickname(Long sellerId, String nickname);
}
