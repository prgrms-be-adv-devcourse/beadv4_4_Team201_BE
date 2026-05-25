package app.giftify.payment.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.PaymentDetailQuery;
import app.giftify.payment.application.inbound.PaymentDetailResult;
import app.giftify.payment.application.inbound.PaymentHistoryQuery;
import app.giftify.payment.application.inbound.PaymentSummaryResult;
import app.giftify.payment.application.inbound.QueryPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.support.common.api.paging.Page;
import app.giftify.support.common.api.paging.PageResponse;

@Service
@Transactional(readOnly = true)
public class QueryPaymentService implements QueryPaymentUseCase {
	private final PaymentRepository paymentRepository;

	public QueryPaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Override
	public PaymentDetailResult getPayment(PaymentDetailQuery query) {
		Payment payment = paymentRepository.findById(query.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[QueryPaymentService] 결제를 찾을 수 없습니다. paymentId=" + query.paymentId()
			));

		// 권한 검증
		if (!payment.isOwnedBy(query.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[QueryPaymentService] 결제 조회 권한이 없습니다."
			);
		}

		return PaymentDetailResult.from(payment);
	}

	@Override
	public PageResponse<PaymentSummaryResult> getPaymentHistory(PaymentHistoryQuery query) {
		Page<Payment> paymentPage = paymentRepository.findByMemberId(
			query.memberId(),
			query.pageRequest()
		);

		List<PaymentSummaryResult> content = paymentPage.content().stream()
			.map(PaymentSummaryResult::from)
			.toList();

		return PageResponse.of(
			content,
			query.pageRequest().page(),
			query.pageRequest().size(),
			paymentPage.totalElements()
		);
	}
}
