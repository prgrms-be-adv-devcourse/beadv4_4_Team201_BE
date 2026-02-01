package app.giftify.product.application.support;

import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class ProductSupport {
    private final ProductRepository productRepository;

    public Product findById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    }

    public Product findByIdAndSellerId(Long id, Long sellerId) {
        return productRepository.findByIdAndSellerId(id, sellerId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    }

    public List<Product> findAllById(List<Long> productsIds) {
        return productRepository.findAllById(productsIds);
    }
}
