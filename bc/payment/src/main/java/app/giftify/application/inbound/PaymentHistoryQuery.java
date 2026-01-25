package app.giftify.application.inbound;

import app.giftify.shared.api.paging.PageRequest;

public record PaymentHistoryQuery(
	Long memberId,
	PageRequest pageRequest
) {
	public static PaymentHistoryQuery of(Long memberId, int page, int size) {
		return new PaymentHistoryQuery(memberId, PageRequest.of(page, size));
	}
}
