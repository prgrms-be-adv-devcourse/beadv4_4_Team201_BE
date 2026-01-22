package app.giftify.wishlist.core.domain;

import java.time.LocalDate;

import app.giftify.shared.domain.base.BaseDomainModel;

public class WishlistItem extends BaseDomainModel {
	private final String authSub;
	private final Long productId;
	private WishlistItemStatus wishlistItemStatus;
	private final LocalDate addedAt;

	private WishlistItem(Long id, String authSub, Long productId, WishlistItemStatus wishlistItemStatus) {
		super(id);
		validate(authSub, productId);
		this.authSub = authSub;
		this.productId = productId;
		this.wishlistItemStatus = wishlistItemStatus;
		this.addedAt = LocalDate.now();
	}

	private WishlistItem(Long id, String authSub, Long productId, WishlistItemStatus wishlistItemStatus,
		LocalDate addedAt) {
		super(id);
		validate(authSub, productId);
		this.authSub = authSub;
		this.productId = productId;
		this.wishlistItemStatus = wishlistItemStatus;
		this.addedAt = addedAt;
	}

	// TODO: 도메인 검증 로직 추가

	public String getAuthSub() {
		return authSub;
	}

	public Long getProductId() {
		return productId;
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
			"authSub=" + authSub +
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
		private String authSub;
		private Long productId;
		private WishlistItemStatus wishlistItemStatus;
		private LocalDate addedAt;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder authSub(String authSub) {
			this.authSub = authSub;
			return this;
		}

		public Builder productId(Long productId) {
			this.productId = productId;
			return this;
		}

		public Builder WishlistItemStatus(WishlistItemStatus wishlistItemStatus) {
			this.wishlistItemStatus = wishlistItemStatus;
			return this;
		}

		public Builder addedAt(LocalDate addedAt) {
			this.addedAt = addedAt;
			return this;
		}

		public WishlistItem build() {
			if (addedAt == null) {
				return new WishlistItem(id, authSub, productId, wishlistItemStatus);
			}
			return new WishlistItem(id, authSub, productId, wishlistItemStatus, addedAt);
		}
	}

	private void validate(String authSub, Long productId) {
		if (authSub == null || authSub.isBlank()) {
			throw new IllegalArgumentException("유효하지 않은 authSub 입니다.");
		}
		if (productId == null || productId <= 0) {
			throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
		}
	}
}
