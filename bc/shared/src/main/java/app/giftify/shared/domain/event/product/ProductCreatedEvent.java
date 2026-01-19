package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductCreatedEvent extends BaseDomainEvent {
	private final ProductSnapshot productSnapshot;

	public ProductCreatedEvent(ProductSnapshot productSnapshot) {
		this.productSnapshot = productSnapshot;
	}

	public ProductSnapshot getProductSnapshot() {
		return productSnapshot;
	}
}
