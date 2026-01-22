package app.giftify.shared.domain.event.product;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductReplicaCreationRequestedEvent extends BaseDomainEvent {
	private final Long id;
	private final String name;
	private final int price;
	private String sellerNickName;

	public ProductReplicaCreationRequestedEvent(
		LocalDateTime occurredAt,
		Long id,
		String name,
		int price,
		String sellerNickName
	) {
		super(occurredAt);
		this.id = id;
		this.name = name;
		this.price = price;
		this.sellerNickName = sellerNickName;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public String getSellerNickName() {
		return sellerNickName;
	}
}
