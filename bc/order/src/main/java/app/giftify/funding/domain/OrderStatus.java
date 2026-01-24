package app.giftify.funding.domain;

public enum OrderStatus {

    PAYMENT_PENDING("결제 대기", "결제창은 열렸으나 아직 입금 확인 전 (주문 유효 시간 체크용)"),
    PAID("결제 완료", "결제 성공 및 펀딩 참여 확정"),
    CONFIRMED("주문 확정", "수령자가 구매 확정 클릭 (정산 확정의 근거)"),
    CANCELED("주문 취소", "결제 전 취소 혹은 결제 후 (주문 확정 전) 취소"),
    FAILED("주문 실패", "결제 재시도용 취소"),
    PARTIAL_CANCELED("주문 부분 취소", "결제 전 취소 혹은 결제 후 (주문 확정 전) 부분 취소"),
    REFUNDED("환불 완료", "주문이 환불 처리된 상태");

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

    @Override
    public String toString() {
        return this.name();
    }
}
