package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * Internal API용 결제 정보 Result DTO.
 *
 * <p>다른 BC에서 동기 통신으로 결제 정보를 조회할 때 사용
 * PG사 연동 정보(paymentKey, approveCode)가 복호화된 상태로 제공</p>
 *
 * @param paymentId       결제 ID
 * @param orderId         주문 ID
 * @param idempotencyKey  멱등성 키
 * @param memberId        회원 ID
 * @param status          결제 상태
 * @param type            결제 유형 (일반/펀딩)
 * @param method          결제 수단
 * @param originAmount    원래 결제 금액
 * @param paidAmount      실제 결제 금액
 * @param paymentKey      PG사 결제 키 (복호화됨, 환불/취소 시 필요)
 * @param approveCode     PG사 승인 코드 (복호화됨, 정산 확인용)
 */
public record InternalPaymentResult(
	Long paymentId,
	String orderId,
	String idempotencyKey,
	Long memberId,
	PaymentStatus status,
	PaymentType type,
	PaymentMethod method,
	Money originAmount,
	Money paidAmount,
	String paymentKey,
	String approveCode
) {

	/**
	 * Payment 도메인 객체와 복호화된 PG 정보로 Result를 생성합니다.
	 *
	 * @param payment             Payment 도메인 객체
	 * @param decryptedPaymentKey 복호화된 PG 결제 키
	 * @param decryptedApproveCode 복호화된 PG 승인 코드
	 * @return InternalPaymentResult
	 */
	public static InternalPaymentResult of(
		Payment payment,
		String decryptedPaymentKey,
		String decryptedApproveCode
	) {
		return new InternalPaymentResult(
			payment.getId(),
			payment.getOrderId(),
			payment.getIdempotencyKey(),
			payment.getMemberId(),
			payment.getStatus(),
			payment.getType(),
			payment.getMethod(),
			payment.getOriginAmount(),
			payment.getPaidAmount(),
			decryptedPaymentKey,
			decryptedApproveCode
		);
	}
}
