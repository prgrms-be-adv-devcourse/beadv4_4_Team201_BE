package app.giftify.funding.in.product;

import java.time.LocalDateTime;

import app.giftify.funding.domain.product.Product;
import app.giftify.funding.domain.product.ProductStatus;

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
			product.getSeller().getId(),
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
