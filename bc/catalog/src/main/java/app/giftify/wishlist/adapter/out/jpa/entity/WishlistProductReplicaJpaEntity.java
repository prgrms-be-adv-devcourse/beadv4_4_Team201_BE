package app.giftify.wishlist.adapter.out.jpa.entity;

import java.time.LocalDateTime;

import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlist_product_replica")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistProductReplicaJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, unique = true)
	private Long productId;

	private boolean wishlistAllowed;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Override
	public LocalDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	private String name;
	private int price;
	private String sellerNickname;

	@Builder
	public WishlistProductReplicaJpaEntity(
		Long productId,
		boolean wishlistAllowed,
		LocalDateTime updatedAt,
		String name,
		int price,
		String sellerNickname
	) {
		this.productId = productId;
		this.wishlistAllowed = wishlistAllowed;
		this.updatedAt = updatedAt;
		this.name = name;
		this.price = price;
		this.sellerNickname = sellerNickname;
	}

	public void update(boolean wishlistAllowed, LocalDateTime updatedAt, String name, int price,
		String sellerNickname) {
		this.wishlistAllowed = wishlistAllowed;
		this.updatedAt = updatedAt;
		this.name = name;
		this.price = price;
		this.sellerNickname = sellerNickname;
	}
}