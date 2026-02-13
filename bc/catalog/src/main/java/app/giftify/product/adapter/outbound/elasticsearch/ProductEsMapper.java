package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.domain.Product;

public class ProductEsMapper {

    public static ProductDocument toDocument(Product product, String sellerNickname) {
        return new ProductDocument(
                String.valueOf(product.getId()),
                sellerNickname,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().name(),
                product.getStatus().name(),
                product.getCreatedAt()
        );
    }
}
