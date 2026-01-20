package app.giftify.in.product;

import java.time.LocalDateTime;

import app.giftify.domain.product.Product;

public record ProductDto(
	Long id,
	String sellerNickName,
	String name,
	String description,
	int price,
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
			product.getCreatedAt()
		);
	}
}
