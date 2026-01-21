package app.giftify.in.product;

import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;

import java.time.LocalDateTime;

public record ProductDto(
	Long id,
	String sellerNickName,
	String name,
	String description,
	int price,
	ProductStatus status,
	LocalDateTime createdAt
) {
	public static ProductDto from(Product product) {
		if (product == null)
			return null;

		return new ProductDto(
			product.getId(),
			product.getSeller().getNickname(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
				product.getStatus(),
			product.getCreatedAt()
		);
	}
}
