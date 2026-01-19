package app.giftify.payment.adapter.in.web.payment;

public interface PgWebhookValidator {
	/**
	 * 웹훅 요청의 정당성을 검증합니다.
	 *
	 * @param payload   Request Body 원본
	 * @param timestamp 전송 시간 헤더 값
	 * @param signature 서명 헤더 값
	 * @return 검증 성공 여부
	 */
	boolean validate(String payload, String timestamp, String signature);
}
