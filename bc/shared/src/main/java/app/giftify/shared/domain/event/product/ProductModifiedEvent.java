package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductModifiedEvent extends BaseDomainEvent {
	private final ProductSnapshot productSnapshot;

	public ProductModifiedEvent(ProductSnapshot productSnapshot) {
		this.productSnapshot = productSnapshot;
	}

	public ProductSnapshot getProductSnapshot() {
		return productSnapshot;
	}
}
