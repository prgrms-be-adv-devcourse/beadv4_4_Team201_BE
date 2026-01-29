package app.giftify.payment.domain;

/**
 * 시스템 수준 상수 정의.
 * 스케줄러, 배치 작업 등 사용자가 아닌 시스템이 호출할 때 사용.
 */
public final class SystemConstants {
	private SystemConstants() {}

	/**
	 * 시스템 호출 시 사용하는 requesterId.
	 * 타임아웃 스케줄러, 웹훅 핸들러 등에서 사용.
	 */
	public static final Long SYSTEM_REQUESTER_ID = -1L;
}
