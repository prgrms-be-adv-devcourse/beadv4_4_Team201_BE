package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;

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
                product.getImageKey(),
                product.getCreatedAt()
        );
    }

    public static ProductResult toProductResult(ProductDocument document) {
        return new ProductResult(
                Long.parseLong(document.getId()),
                document.getSellerNickname(),
                document.getName(),
                document.getDescription(),
                document.getPrice(),
                ProductCategory.valueOf(document.getCategory()),
                document.getImageKey(),
                document.getCreatedAt()
        );
    }
}
