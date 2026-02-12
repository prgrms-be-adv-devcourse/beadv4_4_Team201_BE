package app.giftify.product.application.port.out;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.domain.Product;

public interface ProductEsPort {
    ProductDocument save(Product product);

    int syncAll();
}
