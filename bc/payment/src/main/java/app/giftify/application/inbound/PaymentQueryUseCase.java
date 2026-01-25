package app.giftify.application.inbound;

import app.giftify.shared.api.paging.PageResponse;

public interface PaymentQueryUseCase {
	PaymentDetailResult getPayment(PaymentDetailQuery query);
	PageResponse<PaymentSummaryResult> getPaymentHistory(PaymentHistoryQuery query);
}
