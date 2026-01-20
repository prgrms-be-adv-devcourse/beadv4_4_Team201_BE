package app.giftify.wishlist.core.domain;

import app.giftify.wishlist.core.domain.exceptioin.InvalidWishlistVisibilityException;

public enum Visibility {
    PUBLIC("모두에게 공개"),
    PRIVATE("나에게만 공개"),
    FRIENDS_ONLY("친구에게만 공개");

    private String description;

    Visibility(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Visibility from(String value) {
        try {
            return Visibility.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidWishlistVisibilityException(value);
        }
    }
}
