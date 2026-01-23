package app.giftify.wishlist.core.domain;

import java.time.LocalDate;

import app.giftify.shared.domain.base.BaseDomainModel;

public class WishlistItem extends BaseDomainModel {
	private final Long wishlistId; // id로 Aggregate 참조
	private final Long productId;
	private WishlistItemStatus wishlistItemStatus;
	private final LocalDate addedAt;

	private WishlistItem(Long id, Long wishlistId, Long productId, WishlistItemStatus wishlistItemStatus) {
		super(id);
		validate(wishlistId, productId);
		this.wishlistId = wishlistId;
		this.productId = productId;
		this.wishlistItemStatus = wishlistItemStatus;
		this.addedAt = LocalDate.now();
	}

	private WishlistItem(Long id, Long wishlistId, Long productId, WishlistItemStatus wishlistItemStatus,
		LocalDate addedAt) {
		super(id);
		validate(wishlistId, productId);
		this.wishlistId = wishlistId;
		this.productId = productId;
		this.wishlistItemStatus = wishlistItemStatus;
		this.addedAt = addedAt;
	}

	// TODO: 도메인 검증 로직 추가

	public Long getProductId() {
		return productId;
	}

	public Long getWishlistId() {
		return wishlistId;
	}

	public WishlistItemStatus getWishlistItemStatus() {
		return wishlistItemStatus;
	}

	public LocalDate getAddedAt() {
		return addedAt;
	}

	@Override
	public String toString() {
		return "WishlistItem{" +
			"wishlistId=" + wishlistId +
			", productId=" + productId +
			", wishlistItemStatus=" + wishlistItemStatus +
			", addedAt=" + addedAt +
			'}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		// private String authSub;
		// private Wishlist wishlist;
		private Long wishlistId;
		private Long productId;
		private WishlistItemStatus wishlistItemStatus;
		private LocalDate addedAt;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder wishlistId(Long wishlistId) {
			this.wishlistId = wishlistId;
			return this;
		}

		public Builder productId(Long productId) {
			this.productId = productId;
			return this;
		}

		public Builder wishlistItemStatus(WishlistItemStatus wishlistItemStatus) {
			this.wishlistItemStatus = wishlistItemStatus;
			return this;
		}

		public Builder addedAt(LocalDate addedAt) {
			this.addedAt = addedAt;
			return this;
		}

		public WishlistItem build() {
			if (addedAt == null) {
				return new WishlistItem(id, wishlistId, productId, wishlistItemStatus);
			}
			return new WishlistItem(id, wishlistId, productId, wishlistItemStatus, addedAt);
		}
	}

	private void validate(Long wishlistId, Long productId) {
		if (wishlistId == null || wishlistId <= 0) {
			throw new IllegalArgumentException("유효하지 않은 위시리스트 ID 입니다.");
		}
		if (productId == null || productId <= 0) {
			throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
		}
	}
}
