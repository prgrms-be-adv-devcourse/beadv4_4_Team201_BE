package app.giftify.settlement.domain;

public enum SettlementItemType {
    ITEM_PAYMENT("일반 상품 판매 정산"),
    DEDUCTION_REFUND("환불 차감 금액");

    private final String description;

    SettlementItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}