package app.giftify.settlement.domain;

public enum SettlementItemType {
    ITEM_PAYMENT,         // 일반 상품 판매에 대한 정산금 지급
    DEDUCTION_REFUND      // 고객 환불로 인해 판매자로부터 회수해야 할 금액 차감
}
