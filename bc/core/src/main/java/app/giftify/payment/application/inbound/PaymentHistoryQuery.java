package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.api.paging.PageRequest;

public record PaymentHistoryQuery(
	Long memberId,
	PageRequest pageRequest
) {
	public PaymentHistoryQuery {
		if (memberId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentHistoryQuery] memberId는 필수입니다.");
		}
		if (pageRequest == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[PaymentHistoryQuery] pageRequest는 필수입니다.");
		}
	}

	public static PaymentHistoryQuery of(Long memberId, int page, int size) {
		return new PaymentHistoryQuery(memberId, PageRequest.of(page, size));
	}
}
