package app.giftify.orderDemo.domain;

public enum OrderStatus {
    CREATED,        // 주문 생성됨 (아직 결제 전)
    PAID,           // 결제 완료
    CONFIRMED,      // 구매 확정 (정산 가능)
    CANCEL_PENDING,
    CANCELED        // 주문 전체 취소
}