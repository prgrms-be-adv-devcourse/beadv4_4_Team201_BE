package app.giftify.wishlist.core.domain;

import app.giftify.wishlist.core.domain.exception.InvalidWishlistItemStatusException;

public enum WishlistItemStatus {
    PENDING("펀딩이 시작되지 않은 상품입니다."),
    IN_PROGRESS("펀딩이 진행중인 상품입니다."),
    REQUESTED_CONFIRM("펀딩이 달성되어 수령자 수락 대기 중인 상품입니다."),
    COMPLETED("달성된 펀딩을 수령자가 수락한 상품입니다.");

    private final String description;

    WishlistItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static WishlistItemStatus from(String value) {
        try {
            return WishlistItemStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidWishlistItemStatusException(value);
        }
    }
}
