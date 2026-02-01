package app.giftify.product.application.port.in;

import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.application.support.ProductSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRejectUseCase {
    private final ProductSupport productSupport;

    public void rejectProduct(Long id) {
        Product product = productSupport.findById(id);
        product.reject();
    }
}
