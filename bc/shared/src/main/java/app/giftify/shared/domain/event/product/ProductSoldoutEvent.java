package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductSoldoutEvent extends BaseDomainEvent {
	private final Long productId;

	public ProductSoldoutEvent(Long productId) {
		this.productId = productId;
	}

	public Long getProductId() {
		return productId;
	}
}
