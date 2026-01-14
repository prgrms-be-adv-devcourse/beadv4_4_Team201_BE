package payment.usecase;

import java.util.List;

import payment.dto.response.PaymentDetailResponse;
import payment.dto.response.PaymentSummaryResponse;
import payment.usecase.query.PaymentDetailQuery;
import payment.usecase.query.PaymentHistoryQuery;

public interface PaymentQueryUseCase {
	PaymentDetailResponse getPayment(PaymentDetailQuery query);

	List<PaymentSummaryResponse> getPaymentHistory(PaymentHistoryQuery query);
}
