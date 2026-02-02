package app.giftify.product.adapter.inbound.web.responseDto;

import app.giftify.product.application.port.in.ProductUpdateResult;
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
    public static ProductUpdateResponseDto from(ProductUpdateResult result) {
        return new ProductUpdateResponseDto(
                result.id(),
                result.sellerId(),
                result.name(),
                result.description(),
                result.price(),
                result.stock(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
