package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductSaleEnabledEvent extends BaseDomainEvent {
	private final Long productId;

	public ProductSaleEnabledEvent(Long productId) {
		this.productId = productId;
	}

	public Long getProductId() {
		return productId;
	}
}
