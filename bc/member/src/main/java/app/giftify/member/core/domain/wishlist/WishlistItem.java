package app.giftify.member.core.domain.wishlist;

import app.giftify.shared.domain.base.BaseDomainModel;

import java.time.LocalDate;

public class WishlistItem extends BaseDomainModel {
    private final Long wishlistId;
    private final Long productId;
    private ItemStatus itemStatus;
    private final LocalDate addedAt;

    private WishlistItem(Long id, Long wishlistId, Long productId, ItemStatus itemStatus) {
        super(id);
        validate(wishlistId, productId);
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.itemStatus = itemStatus;
        this.addedAt = LocalDate.now();
    }

    private WishlistItem(Long id, Long wishlistId, Long productId, ItemStatus itemStatus, LocalDate addedAt) {
        super(id);
        validate(wishlistId, productId);
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.itemStatus = itemStatus;
        this.addedAt = addedAt;
    }

    // TODO: 도메인 검증 로직 추가

    public Long getWishlistId() {
        return wishlistId;
    }

    public Long getProductId() {
        return productId;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    public LocalDate getAddedAt() {
        return addedAt;
    }

    @Override
    public String toString() {
        return "WishlistItem{" +
                "wishlistId=" + wishlistId +
                ", productId=" + productId +
                ", itemStatus=" + itemStatus +
                ", addedAt=" + addedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long wishlistId;
        private Long productId;
        private ItemStatus itemStatus;
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

        public Builder itemStatus(ItemStatus itemStatus) {
            this.itemStatus = itemStatus;
            return this;
        }

        public Builder addedAt() {
            this.addedAt = LocalDate.now();
            return this;
        }

        public WishlistItem build() {
            if (addedAt == null) {
                return new WishlistItem(id, wishlistId, productId, itemStatus);
            }
            return new WishlistItem(id, wishlistId, productId, itemStatus, addedAt);
        }
    }

    private void validate(Long wishlistId, Long productId) {
        if (wishlistId == null || wishlistId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 위시리스트 ID입니다.");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다.");
        }
    }
}
