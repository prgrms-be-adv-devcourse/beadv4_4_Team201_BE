package app.giftify.shared.domain.type;

public enum TargetType {
    PRODUCT("일반 상품"),
    FUNDING("펀딩 상품"),
    DEPOSIT("예치금"),
    COUPON("쿠폰");

    private final String description;

    TargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
