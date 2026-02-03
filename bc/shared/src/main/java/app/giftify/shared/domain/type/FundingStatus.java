package app.giftify.shared.domain.type;

public enum FundingStatus {
    IN_PROGRESS,    // 펀딩 진행 중
    ACHIEVED,       // 목표액 달성 및 수령자 확정 대기 중
    ACCEPTED,       // 수령자 확정
    REFUSED,       // 수령자 거절
    EXPIRED,        // 기한 만료
    CLOSED          // 시스템에 의한 펀딩 종료 OR 수령자가 2주 내 확정 누르지 않아 종료된 상태
}
