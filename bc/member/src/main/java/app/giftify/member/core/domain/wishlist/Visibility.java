package app.giftify.member.core.domain.wishlist;

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
}
