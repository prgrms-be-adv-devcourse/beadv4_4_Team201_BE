package app.giftify.wishlist.core.domain.replica;

import app.giftify.shared.domain.base.BaseDomainModel;

import java.time.Duration;
import java.time.LocalDateTime;

public class WishlistProductReplica extends BaseDomainModel {

    private final Long productId;
    private String name;
    private int price;
    private String sellerNickName;
    private boolean saleStatus;
    private LocalDateTime updatedAt;

    private WishlistProductReplica(
            Long id,
            Long productId,
            String name,
            int price,
            String sellerNickName,
            boolean wishlistAllowed,
            LocalDateTime updatedAt
    ) {
        super(id);
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.sellerNickName = sellerNickName;
        this.saleStatus = wishlistAllowed;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public boolean isWishlistAllowed() {
        return saleStatus;
    }

    public Long getProductId() {
        return productId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void enableSale() {
        this.saleStatus = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disableSale() {
        this.saleStatus = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, int price, String sellerNickName) {
        this.name = name;
        this.price = price;
        this.sellerNickName = sellerNickName;
        this.updatedAt = LocalDateTime.now();
    }

    // [ updatedAt + ttl > now ] -> [true] -> fresh
    // [ updatedAt + ttl <= now] -> [false] -> stale : 오래된 상품
    // 외부 시간 정책(ttl)은 주입 받음
    public boolean isFresh(Duration ttl) {
        return updatedAt.plus(ttl).isAfter(LocalDateTime.now());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long productId;
        private String name;
        private int price;
        private String sellerNickName;
        private boolean wishlistAllowed;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(int price) {
            this.price = price;
            return this;
        }

        public Builder sellerNickName(String sellerNickName) {
            this.sellerNickName = sellerNickName;
            return this;
        }

        public Builder wishlistAllowed(boolean wishlistAllowed) {
            this.wishlistAllowed = wishlistAllowed;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public WishlistProductReplica build() {
            return new WishlistProductReplica(
                    id,
                    productId,
                    name,
                    price,
                    sellerNickName,
                    wishlistAllowed,
                    updatedAt
            );
        }
    }
}

