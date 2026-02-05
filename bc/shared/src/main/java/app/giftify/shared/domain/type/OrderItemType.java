package app.giftify.shared.domain.type;

public enum OrderItemType {
    NORMAL_ORDER("일반 결제형"),
    FUNDING_GIFT("펀딩형 선물"),
    NORMAL_GIFT("일반 선물형"),
    ;

    private String description;

    OrderItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
