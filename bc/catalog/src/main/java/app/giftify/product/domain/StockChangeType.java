package app.giftify.product.domain;

public enum StockChangeType {
    MANUAL_SYSTEM, // 관리자 수동 조정 (시스템 장애, 운영 이슈 등)
    MANUAL_SELLER, // 판매자 수동 수정
    ORDER_COMPLETED, // 주문 성공으로 차감
    ORDER_REFUNDED // 주문 취소/환불 복구
}
