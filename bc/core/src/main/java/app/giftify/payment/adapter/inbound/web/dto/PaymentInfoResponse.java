package app.giftify.payment.adapter.inbound.web.dto;

import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * Internal API용 결제 정보 응답 DTO.
 *
 * <p>다른 BC에서 동기 통신으로 결제 정보를 조회할 때 사용
 * PG사 연동 정보(paymentKey, approveCode)가 복호화된 상태로 제공.</p>
 *
 * <h3>보안 주의사항</h3>
 * <p>이 DTO는 Internal API 전용입니다. 외부 API에서는 paymentKey, approveCode를
 * 제외한 별도의 응답 DTO를 사용해야 합니다.</p>
 *
 * @param paymentId      결제 ID
 * @param orderId        주문 ID
 * @param idempotencyKey 멱등성 키
 * @param memberId       회원 ID
 * @param status         결제 상태
 * @param type           결제 유형 (일반/펀딩)
 * @param method         결제 수단
 * @param originAmount   원래 결제 금액
 * @param paidAmount     실제 결제 금액
 * @param paymentKey     PG사 결제 키 (복호화됨, 환불/취소 시 필요)
 * @param approveCode    PG사 승인 코드 (복호화됨, 정산 확인용)
 */
public record PaymentInfoResponse(
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
	 * InternalPaymentResult로부터 응답 DTO를 생성합니다.
	 *
	 * @param result 복호화된 결제 정보
	 * @return PaymentInfoResponse DTO
	 */
	public static PaymentInfoResponse from(InternalPaymentResult result) {
		return new PaymentInfoResponse(
			result.paymentId(),
			result.orderId(),
			result.idempotencyKey(),
			result.memberId(),
			result.status(),
			result.type(),
			result.method(),
			result.originAmount(),
			result.paidAmount(),
			result.paymentKey(),
			result.approveCode()
		);
	}
}
