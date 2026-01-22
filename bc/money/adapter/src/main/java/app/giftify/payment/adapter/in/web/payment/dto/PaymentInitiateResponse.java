package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import payment.usecase.result.PaymentInitiateResult;

/**
 * 결제 시작 응답 DTO.
 * 결제 시작 결과를 클라이언트에 전달합니다.
 *
 * <p>현재는 예치금 전용 결제만 지원합니다.
 * 예치금 부족 시 예외가 발생하므로, 정상 응답은 예치금으로 완납된 경우만 해당됩니다.
 * 따라서 completed는 항상 true이고, PG 관련 필드들은 모두 null/0입니다.</p>
 *
 * <p>향후 복합 결제 활성화 시:
 * - completed=false, pgPaymentRequired>0, pgOrderId!=null 인 경우가 생깁니다.
 * - 클라이언트는 pgOrderId로 Toss SDK 결제를 진행해야 합니다.</p>
 */
public record PaymentInitiateResponse(
	Long orderId,                 // Order BC의 주문 PK (Long)
	BigDecimal walletUsed,        // 예치금 사용액
	BigDecimal pgPaymentRequired, // PG 결제 필요액 - 현재 항상 0 (복합 결제 미사용)
	Long paymentId,               // Payment PK - 현재 항상 null (복합 결제 미사용)
	String pgOrderId,             // PG용 주문 ID (Toss 규격) - 현재 항상 null (복합 결제 미사용으로 인해 pg 사용 발생하지 않음)
	boolean completed,            // 결제 완료 여부 - 현재 항상 true (예치금 부족 시 예외 발생)
	String orderName              // 주문명 (Toss SDK용)
) {
	private static final String DEFAULT_ORDER_NAME = "Giftify 결제";

	public static PaymentInitiateResponse from(PaymentInitiateResult result) {
		return new PaymentInitiateResponse(
			result.orderId(),
			result.walletUsed().amount(),
			result.pgPaymentRequired().amount(),
			result.paymentId(),
			result.pgOrderId(),
			result.completed(),
			DEFAULT_ORDER_NAME
		);
	}
}
