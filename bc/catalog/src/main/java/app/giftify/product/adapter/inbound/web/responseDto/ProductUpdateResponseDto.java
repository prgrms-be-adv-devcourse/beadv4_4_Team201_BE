package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.adapter.outbound.jpa.entity.Product;
import app.giftify.product.domain.ProductStatus;

import java.time.LocalDateTime;

public record ProductUpdateResponseDto(
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
    public static ProductUpdateResponseDto from(Product product) {
        return new ProductUpdateResponseDto(
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
