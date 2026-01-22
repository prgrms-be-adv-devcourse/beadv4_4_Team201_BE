package app.giftify.order.domain.domain;

public enum OrderStatus {

    PAYMENT_PENDING("주문 대기", "결제창은 열렸으나 아직 입금 확인 전 (주문 유효 시간 체크용)"),
    ORDERED("주문 결제 완료", "결제 성공 / 펀딩 참여 확정"),
    CONFIRMED("주문 확정", "수령자가 구매 확정 클릭 (정산 확정의 근거)"),
    CANCELED("주문 취소", "결제 전 취소 혹은 결제 후 (확정 전) 취소");

    private final String description;
    private final String detail;

    OrderStatus(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }

    public String getDescription() {
        return description;
    }
    public String getDetail() {
        return detail;
    }
}
