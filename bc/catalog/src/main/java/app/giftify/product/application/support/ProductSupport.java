package app.giftify.product.application.support;

import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.product.domain.exception.ProductNotActiveException;
import app.giftify.product.domain.exception.ProductOutOfStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class ProductSupport {
    private final ProductRepositoryPort productRepositoryPort;

    public Product findById(Long id) {
        return productRepositoryPort.findById(id).orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    }

    public Product findByIdAndSellerId(Long id, Long sellerId) {
        return productRepositoryPort.findByIdAndSellerId(id, sellerId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    }

    public List<Product> findAllById(List<Long> productsIds) {
        return productRepositoryPort.findAllById(productsIds);
    }

    // 상품 검증 - 상태가 ACTIVE이고, 재고 !=0 인 상품
    public void validatePurchasable(List<Product> products) {
        for (Product product : products) {
            if (product.getStatus() != ProductStatus.ACTIVE)
                throw new ProductNotActiveException(product.getId());
            if (product.getStock() == 0)
                throw new ProductOutOfStockException(product.getId());
        }
    }
}
