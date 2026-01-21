package domain.settlement;

public enum SettlementStatus {
    READY,      // 정산 대기
    EXPECTED,   // 정산 확정
    PAID,       // 지급 완료
    CANCELLED   // 정산 취소
}
