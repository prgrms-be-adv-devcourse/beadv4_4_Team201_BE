package app.giftify.wishlist.core.domain;

import app.giftify.shared.domain.base.BaseDomainModel;

import java.time.LocalDate;

public class WishlistItem extends BaseDomainModel {
    private final String authSub;
    private final Long productId;
    private ItemStatus itemStatus;
    private final LocalDate addedAt;

    private WishlistItem(Long id, String authSub, Long productId, ItemStatus itemStatus) {
        super(id);
        validate(authSub, productId);
        this.authSub = authSub;
        this.productId = productId;
        this.itemStatus = itemStatus;
        this.addedAt = LocalDate.now();
    }

    private WishlistItem(Long id, String authSub, Long productId, ItemStatus itemStatus, LocalDate addedAt) {
        super(id);
        validate(authSub, productId);
        this.authSub = authSub;
        this.productId = productId;
        this.itemStatus = itemStatus;
        this.addedAt = addedAt;
    }

    // TODO: 도메인 검증 로직 추가

    public String getAuthSub() {
        return authSub;
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
                "authSub=" + authSub +
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
        private String authSub;
        private Long productId;
        private ItemStatus itemStatus;
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
                return new WishlistItem(id, authSub, productId, itemStatus);
            }
            return new WishlistItem(id, authSub, productId, itemStatus, addedAt);
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
