package app.giftify.product.adapter.inbound;

import java.time.LocalDateTime;

import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;

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
