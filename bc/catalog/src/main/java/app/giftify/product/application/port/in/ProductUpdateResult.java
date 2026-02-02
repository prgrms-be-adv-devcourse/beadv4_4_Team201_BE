package app.giftify.product.application.port.in;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;

import java.time.LocalDateTime;

/**
 * 상품 수정(Update) UseCase의 결과 전용 객체
 */
public record ProductUpdateResult(
        Long id,
        Long sellerId,
        String name,
        String description,
        int price,
        int stock,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductUpdateResult from(Product product) {
        return new ProductUpdateResult(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
