package app.giftify.product.application.port.in;

import java.time.LocalDateTime;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductStatus;

/**
 * 애플리케이션 계층의 조회 결과 전용 객체 (Query Model)
 * 외부 어댑터 계층(Web)의 DTO와 분리하여 애플리케이션의 독립성을 보장한다.
 */
public record MyProductResult(
    Long id,
    String name,
    String description,
    int price,
    int stock,
    ProductStatus status,
    ProductCategory category,
    String imageKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MyProductResult of(Product product) {
        return new MyProductResult(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getCategory(),
                product.getImageKey(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
