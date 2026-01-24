package payment.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import domain.payment.Payment;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentRefundUseCase;
import payment.usecase.command.RefundPaymentCommand;

/**
 * 결제 환불 처리 핸들러.
 * OrderCanceledEvent 수신 시 해당 주문의 결제를 환불 처리합니다.
 */
@Component
public class PaymentRefundHandler {

	private static final Logger log = LoggerFactory.getLogger(PaymentRefundHandler.class);
	private static final String REFUND_REASON_ORDER_CANCELED = "주문 취소로 인한 환불";

	private final PaymentRefundUseCase paymentRefundUseCase;
	private final PaymentRepository paymentRepository;

	public PaymentRefundHandler(
		PaymentRefundUseCase paymentRefundUseCase,
		PaymentRepository paymentRepository
	) {
		this.paymentRefundUseCase = paymentRefundUseCase;
		this.paymentRepository = paymentRepository;
	}

	/**
	 * 주문 취소에 따른 결제 환불 처리.
	 *
	 * @param orderUuid 취소된 주문의 UUID
	 * @param reason 환불 사유 (null이면 기본 사유 사용)
	 */
	public void handleOrderCanceled(String orderUuid, String reason) {
		log.info("[PaymentRefund] 주문 취소 환불 처리 시작. orderUuid={}", orderUuid);

		List<Payment> payments = paymentRepository.findByOrderUuid(orderUuid);

		if (payments.isEmpty()) {
			log.info("[PaymentRefund] 환불할 결제 없음. orderUuid={}", orderUuid);
			return;
		}

		String refundReason = (reason != null) ? reason : REFUND_REASON_ORDER_CANCELED;

		for (Payment payment : payments) {
			try {
				RefundPaymentCommand command = new RefundPaymentCommand(
					payment.getPaymentId(),
					refundReason
				);
				paymentRefundUseCase.refund(command);
			} catch (Exception e) {
				log.error("[PaymentRefund] 결제 환불 실패. paymentId={}, orderUuid={}",
					payment.getPaymentId(), orderUuid, e);
			}
		}

		log.info("[PaymentRefund] 주문 취소 환불 처리 완료. orderUuid={}, processedCount={}",
			orderUuid, payments.size());
	}
}
