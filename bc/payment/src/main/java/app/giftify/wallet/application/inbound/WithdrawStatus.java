package app.giftify.wallet.application.inbound;

/**
 * 출금 상태
 */
public enum WithdrawStatus {
	PENDING,    // 출금 처리 대기
	COMPLETED,  // 출금 완료
	FAILED      // 출금 실패
}
