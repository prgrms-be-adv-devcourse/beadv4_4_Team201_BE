package app.giftify.shared.domain.event.product;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class ProductReplicaUpdatedEvent extends BaseDomainEvent {
	private final Long id;
	private final String name;
	private final int price;
	private final String sellerNickname;
	// private final boolean 상품구매가능/불가능

	public ProductReplicaUpdatedEvent(
		LocalDateTime occurredAt,
		Long id,
		String name,
		int price,
		String sellerNickname
	) {
		super(occurredAt);
		this.id = id;
		this.name = name;
		this.price = price;
		this.sellerNickname = sellerNickname;
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

	public String getSellerNickname() {
		return sellerNickname;
	}
}
