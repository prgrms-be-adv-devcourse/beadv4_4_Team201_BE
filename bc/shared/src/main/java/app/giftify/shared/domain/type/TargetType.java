package app.giftify.shared.domain.type;

public enum TargetType {
    GENERAL_PRODUCT("일반 상품"),       // 일반 결제형 상품
    FUNDING_PENDING("펀딩 예정 상품"),   // 첫 참여 펀딩
    FUNDING("펀딩 상품")
    ;

    private final String description;

    TargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
