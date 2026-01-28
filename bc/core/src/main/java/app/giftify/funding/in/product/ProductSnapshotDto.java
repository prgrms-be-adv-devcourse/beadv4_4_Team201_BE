package app.giftify.funding.in.product;

import java.time.LocalDateTime;

import app.giftify.funding.domain.product.ProductSnapshot;

public record ProductSnapshotDto(
	Long snapshotId,
	Long originalProductId,
	Long sellerId,
	String sellerNickname,
	String name,
	String description,
	int price,
	boolean onSale, // product.getStatus() == ACTIVE && product.getStock() != 0
	LocalDateTime createdAt
	// quantity (todo 일반결제)
) {
	public static ProductSnapshotDto from(ProductSnapshot productSnapshot) {
		if (productSnapshot == null)
			return null;

		return new ProductSnapshotDto(
			productSnapshot.getId(), // 주문 모듈의 targetId
			productSnapshot.getOriginalProductId(),
			productSnapshot.getSellerId(),
			productSnapshot.getSellerNickname(),
			productSnapshot.getName(),
			productSnapshot.getDescription(),
			productSnapshot.getPrice(),
			productSnapshot.isOnSale(),
			productSnapshot.getCreatedAt()
		);
	}
}
