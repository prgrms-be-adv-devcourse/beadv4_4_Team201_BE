package app.giftify.payment.application.inbound;

import app.giftify.support.common.api.paging.PageResponse;

public interface QueryPaymentUseCase {
	PaymentDetailResult getPayment(PaymentDetailQuery query);
	PageResponse<PaymentSummaryResult> getPaymentHistory(PaymentHistoryQuery query);
}
