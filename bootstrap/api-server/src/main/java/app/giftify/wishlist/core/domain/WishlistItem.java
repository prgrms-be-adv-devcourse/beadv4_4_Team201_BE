package app.giftify.wishlist.core.domain;

import app.giftify.support.common.base.BaseDomainModel;
import app.giftify.wishlist.core.domain.exception.WishlistItemInvalidStatusTransitionException;
import com.mysema.commons.lang.Assert;

import java.time.LocalDateTime;

public class WishlistItem extends BaseDomainModel {
    private final Long wishlistId; // id로 Aggregate 참조
    private final Long productId;
    private WishlistItemStatus wishlistItemStatus;
    private final LocalDateTime addedAt;

    private WishlistItem(Long id, Long wishlistId, Long productId, WishlistItemStatus wishlistItemStatus) {
        super(id);
        validate(wishlistId, productId);
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.wishlistItemStatus = wishlistItemStatus;
        this.addedAt = LocalDateTime.now();
    }

    private WishlistItem(Long id, Long wishlistId, Long productId, WishlistItemStatus wishlistItemStatus,
                         LocalDateTime addedAt) {
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

    public LocalDateTime getAddedAt() {
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

    /**
     * 도메인 빌더
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long wishlistId;
        private Long productId;
        private WishlistItemStatus wishlistItemStatus;
        private LocalDateTime addedAt;

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

        public Builder addedAt(LocalDateTime addedAt) {
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

    /**
     * 위시리스트아이템 생성 시 값 검증
     *
     * @param wishlistId
     * @param productId
     */
    private void validate(Long wishlistId, Long productId) {
        Assert.notNull(wishlistId, "위시리스트 ID는 null일 수 없습니다.");
        Assert.isTrue(wishlistId > 0, "유효하지 않은 위시리스트 ID 입니다.");

        Assert.notNull(productId, "상품 ID는 null일 수 없습니다.");
        Assert.isTrue(productId > 0, "유효하지 않은 상품 ID입니다.");
    }


    /**
     * 위시리스트아이템 상태 검증 및 변경
     */
    public void changeStatus(WishlistItemStatus targetStatus) {
        if (this.wishlistItemStatus == targetStatus)
            return;

        if (!canTransitionTo(targetStatus)) {
            throw new WishlistItemInvalidStatusTransitionException();
        }

        this.wishlistItemStatus = targetStatus;
    }

    private boolean canTransitionTo(WishlistItemStatus next) {
        return switch (this.wishlistItemStatus) {
            case PENDING -> (next == WishlistItemStatus.IN_PROGRESS);

            case IN_PROGRESS -> (next == WishlistItemStatus.REQUESTED_CONFIRM);

            case REQUESTED_CONFIRM -> (
                    next == WishlistItemStatus.COMPLETED   // 수락
                            || next == WishlistItemStatus.PENDING     // 2주 경과로 원복
            );
            case COMPLETED -> false;     // (정책) 완료 후 바로 삭제 or 히스토리로 이동
        };
    }
}
