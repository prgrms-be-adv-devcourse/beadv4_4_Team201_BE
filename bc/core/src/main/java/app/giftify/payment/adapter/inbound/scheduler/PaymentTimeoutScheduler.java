package app.giftify.payment.adapter.inbound.scheduler;

import static app.giftify.payment.domain.SystemConstants.SYSTEM_REQUESTER_ID;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTimeoutScheduler {

	private static final String TIMEOUT_REASON = "TIMEOUT";

	private final CancelPaymentUseCase cancelPaymentUseCase;
	private final PaymentRepository paymentRepository;

	@Value("${payment.timeout.minutes:30}")
	private int timeoutMinutes;

	@Scheduled(fixedDelayString = "${payment.timeout.check-interval:60000}")
	public void cancelExpiredPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
		List<Payment> expiredPayments = paymentRepository.findPendingPaymentsBefore(threshold);

		if (expiredPayments.isEmpty()) {
			log.debug("[Payment] 만료된 결제가 없습니다. threshold={}", threshold);
			return;
		}

		log.info("[Payment] 만료된 결제 {}건 자동 취소 시작. threshold={}", expiredPayments.size(), threshold);

		for (Payment payment : expiredPayments) {
			try {
				CancelPaymentCommand command = new CancelPaymentCommand(
					payment.getId(),
					SYSTEM_REQUESTER_ID,
					TIMEOUT_REASON
				);
				cancelPaymentUseCase.cancel(command);
				log.info("[Payment] 결제 타임아웃 처리 완료. paymentId={}", payment.getId());
			} catch (Exception e) {
				log.error("[Payment] 결제 자동 취소 실패. paymentId={}", payment.getId(), e);
			}
		}
	}
}
