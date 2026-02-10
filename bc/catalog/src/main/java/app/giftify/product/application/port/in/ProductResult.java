package app.giftify.product.application.port.in;

import java.time.LocalDateTime;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;

/**
 * 애플리케이션 계층의 조회 결과 전용 객체 (Query Model)
 * 외부 어댑터 계층(Web)의 DTO와 분리하여 애플리케이션의 독립성을 보장한다.
 */
public record ProductResult(
    Long id,
    String sellerNickName,
    String name,
    String description,
    int price,
    ProductCategory category,
    String imageKey,
    LocalDateTime createdAt
) {
    public static ProductResult of(Product product, String sellerNickname) {
        return new ProductResult(
                product.getId(),
                sellerNickname,
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageKey(),
                product.getCreatedAt()
        );
    }
}
