package app.giftify.orderDemo.domain;

public enum OrderItemStatus {
    CREATED,    // 주문 대기(생성)
    PAID,  // 주문 완료(결제 완료)
    CANCELLED,  // 주문 취소
}
