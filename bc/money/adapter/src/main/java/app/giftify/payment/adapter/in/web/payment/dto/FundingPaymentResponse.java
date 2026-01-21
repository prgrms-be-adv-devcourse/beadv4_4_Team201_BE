package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import payment.usecase.result.FundingContributeResult;

/**
 * 펀딩 참여 결제 응답 DTO.
 * 복합 결제 결과를 클라이언트에 전달합니다.
 */
public record FundingPaymentResponse(
	BigDecimal walletUsed,        // 예치금 사용액
	BigDecimal pgPaymentRequired, // PG 결제 필요액 (0이면 예치금으로 완납)
	Long paymentId,               // PG 결제 필요 시 Payment ID (없으면 null)
	String orderId,               // PG 결제 필요 시 Order ID (없으면 null)
	boolean completed,            // 결제 완료 여부 (예치금으로 완납 시 true)
	String orderName              // 주문명 (Toss SDK용)
) {
	private static final String DEFAULT_ORDER_NAME = "Giftify 펀딩 참여";

	public static FundingPaymentResponse from(FundingContributeResult result) {
		return new FundingPaymentResponse(
			result.walletUsed().amount(),
			result.pgPaymentRequired().amount(),
			result.paymentId(),
			result.orderId(),
			result.completed(),
			DEFAULT_ORDER_NAME
		);
	}
}
