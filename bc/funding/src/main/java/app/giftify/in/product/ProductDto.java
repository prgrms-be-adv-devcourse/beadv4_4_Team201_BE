package app.giftify.in.product;

import java.time.LocalDateTime;

public record ProductDto(
	Long id,
	String sellerNickName,
	String name,
	String description,
	int price,
	int stock,
	LocalDateTime createdAt
) {
}
