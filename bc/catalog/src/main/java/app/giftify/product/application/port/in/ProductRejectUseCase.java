package app.giftify.product.application.port.in;

import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductRejectUseCase {
    private final ProductRepositoryPort productRepositoryPort;

    @Transactional
    public void rejectProduct(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));

        product.reject();
        productRepositoryPort.save(product);
    }
}
