package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductSaleDisabledEvent extends BaseDomainEvent {
	private final Long productId;
	private final ProductSaleDisableReason reason;

	public ProductSaleDisabledEvent(Long productId, ProductSaleDisableReason reason) {
		this.productId = productId;
		this.reason = reason;
	}

	public Long getProductId() {
		return productId;
	}
}
