package app.giftify.shared.domain.event.product;

import java.time.LocalDateTime;

public record ProductSnapshot(
	Long id,
	String sellerNickName,
	String name,
	String description,
	int price,
	int stock,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}

