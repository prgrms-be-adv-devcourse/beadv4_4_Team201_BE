package domain.settlement;

public enum SettlementItemStatus {
    PENDING,     // 주문 생성
    ACCUMULATED, // 결제 완료
    READY,       // 정산 대기
    IN_PROGRESS, // 정산 중
    COMPLETED,
    CANCELLED
}
