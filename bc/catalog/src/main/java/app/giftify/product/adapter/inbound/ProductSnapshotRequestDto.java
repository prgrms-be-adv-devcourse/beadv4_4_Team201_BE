package app.giftify.product.adapter.inbound;

import java.util.List;

public record ProductSnapshotRequestDto(
	List<SnapshotItem> items
) {
	public record SnapshotItem(
		Long productId
		// Integer quantity // 펀딩이 아닌 일반주문일 때 quantity 필요
	) {
	}
}
