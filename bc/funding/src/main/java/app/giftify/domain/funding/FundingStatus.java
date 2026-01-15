package app.giftify.domain.funding;

public enum FundingStatus {
	IN_PROGRESS,    // 펀딩 진행 중
	ACHIEVED,       // 목표액 달성
	EXPIRED,        // 기한 만료
	CLOSED          // 시스템에 의한 펀딩 종료 OR 수령자가 2주 내 확정 누르지 않아 종료된 상태
}
