package app.giftify.payment.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.BulkPaymentAmountUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.support.common.api.exception.BusinessException;
import app.giftify.support.common.money.Money;
import lombok.RequiredArgsConstructor;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BulkPaymentQueryService implements BulkPaymentAmountUseCase {
	private static final Logger log = LoggerFactory.getLogger(BulkPaymentQueryService.class);


	private static final List<PaymentStatus> SETTLED_STATUSES = List.of(
		PaymentStatus.PAID, PaymentStatus.CANCELED
	);

	private final PaymentRepository paymentRepository;

	@Override
	public Map<Long, Money> getBulkAmounts(List<Long> orderIds) {
		List<Payment> payments = paymentRepository.findAllByOrderIdInAndStatusIn(orderIds, SETTLED_STATUSES);

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
}
