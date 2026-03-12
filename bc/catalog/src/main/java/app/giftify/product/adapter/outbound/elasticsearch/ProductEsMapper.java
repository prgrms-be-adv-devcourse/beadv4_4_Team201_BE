package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductStatus;

public class ProductEsMapper {

    public static ProductDocument toDocument(Product product, String sellerNickname, float[] embedding) {
        return new ProductDocument(
                String.valueOf(product.getId()),
                sellerNickname,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().name(),
                product.getStatus().name(),
                product.getImageKey(),
                product.getCreatedAt(),
                embedding
        );
    }

    // 프론트 반환용 도큐먼트 -> ProductResult
    public static ProductResult toProductResult(ProductDocument document, Product product) {
        return new ProductResult(
                Long.parseLong(document.getId()),
                document.getSellerNickname(),
                document.getName(),
                document.getDescription(),
                document.getPrice(),
                ProductCategory.valueOf(document.getCategory()),
                product.getImageKey(),
                product.getStock() == 0,      // RDB의 최신 품절 여부
                product.getStatus() == ProductStatus.ACTIVE, // RDB의 최신 활성화 여부
                document.getCreatedAt()
        );
    }
}