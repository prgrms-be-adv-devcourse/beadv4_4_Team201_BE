package app.giftify.shared.domain.event.product;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductSnapshotCreationRequestedEvent extends BaseDomainEvent {
	private final Long id;
	private final String name;
	private final String description;
	private final int price;
	private final String sellerNickName;

	public ProductSnapshotCreationRequestedEvent(Long id, String name, String description, int price,
		String sellerNickName) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.sellerNickName = sellerNickName;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public int getPrice() {
		return price;
	}

	public String getSellerNickName() {
		return sellerNickName;
	}
}
