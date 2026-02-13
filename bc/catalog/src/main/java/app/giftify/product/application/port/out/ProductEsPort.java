package app.giftify.product.application.port.out;

import app.giftify.product.domain.Product;

public interface ProductEsPort {
    void save(Product product);

    int syncAll();
}
