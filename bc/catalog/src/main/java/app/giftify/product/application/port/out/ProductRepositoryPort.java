package app.giftify.product.application.port.out;

import app.giftify.product.domain.Product;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);

    Optional<Product> findById(Long productId);

    Optional<Product> findByIdAndSellerId(Long productId, Long sellerId);

    /**
     * QueryDSL
     */
    Page<Product> searchProducts(ProductSearchCommand command);

    Page<Product> searchMyProducts(Long sellerId, MyProductSearchCommand command);

    List<Product> findAllById(List<Long> productsIds);

    Optional<Product> findByIdForUpdate(Long productId);
}
