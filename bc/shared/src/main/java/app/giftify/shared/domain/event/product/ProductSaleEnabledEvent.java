package app.giftify.shared.domain.event.product;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductSaleEnabledEvent extends BaseDomainEvent {
	private final Long productId;

	public ProductSaleEnabledEvent(LocalDateTime occurredAt, Long productId) {
		super(occurredAt);
		this.productId = productId;
	}

	public Long getProductId() {
		return productId;
	}
}
