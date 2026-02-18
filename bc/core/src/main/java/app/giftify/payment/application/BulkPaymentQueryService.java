package app.giftify.payment.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.BulkPaymentAmountUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.api.exception.BusinessException;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BulkPaymentQueryService implements BulkPaymentAmountUseCase {

	private static final List<PaymentStatus> SETTLED_STATUSES = List.of(
		PaymentStatus.PAID, PaymentStatus.RECEIVED, PaymentStatus.REFUNDED
	);
	private static final int MAX_RETRIES = 3;
	private static final long RETRY_DELAY_MS = 200;

	private final PaymentRepository paymentRepository;

	@Override
	public Map<Long, Money> getBulkAmounts(List<Long> orderIds) {
		List<Payment> payments = fetchWithRetry(orderIds);

		Map<Long, Money> result = new HashMap<>();
		for (Payment payment : payments) {
			try {
				Money netAmount = payment.getPaidAmount()
					.minus(payment.getRefundedAmount());
				result.merge(payment.getOrderId(), netAmount, Money::plus);
			} catch (BusinessException e) {
				log.warn("[BulkPaymentQuery] orderId={}, error={}",
					payment.getOrderId(), e.getMessage());
			}
		}
		return result;
	}

	private List<Payment> fetchWithRetry(List<Long> orderIds) {
		int attempt = 0;
		while (true) {
			try {
				return paymentRepository.findAllByOrderIdInAndStatusIn(orderIds, SETTLED_STATUSES);
			} catch (TransientDataAccessException e) {
				attempt++;
				if (attempt >= MAX_RETRIES) {
					throw e;
				}
				log.warn("[BulkPaymentQuery] DB 조회 실패, {}회 재시도. error={}", attempt, e.getMessage());
				sleep(RETRY_DELAY_MS * attempt);
			}
		}
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}
}
