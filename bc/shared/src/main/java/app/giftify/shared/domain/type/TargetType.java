package app.giftify.shared.domain.type;

public enum TargetType {
    DIRECT_PURCHASE("일반 구매"),
    DIRECT_GIFT("일반 선물"),
    DIRECT_GIFT_ON_FUNDING("펀딩 중인 상품 선물"),
    FUNDING_PENDING("펀딩 예정 상품"),
    FUNDING("펀딩 상품")
    ;

    private final String description;

    TargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFunding() {
        return this == FUNDING || this == FUNDING_PENDING;
    }
}
