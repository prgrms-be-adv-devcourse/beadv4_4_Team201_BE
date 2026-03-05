package app.giftify.payment.adapter.inbound.scheduler;

import static app.giftify.payment.domain.SystemConstants.*;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
	private static final int BATCH_SIZE = 100;

	private final CancelPaymentUseCase cancelPaymentUseCase;
	private final PaymentRepository paymentRepository;

	@Value("${payment.timeout.minutes:30}")
	private int timeoutMinutes;

	@Scheduled(fixedDelayString = "${payment.timeout.check-interval:600000}")
	public void cancelExpiredPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
		int totalCanceled = 0;

		Slice<Payment> slice;
		do {
			slice = paymentRepository.findPendingPaymentsBefore(threshold, PageRequest.of(0,
				BATCH_SIZE));

			for (Payment payment : slice.getContent()) {
				try {
					CancelPaymentCommand command = CancelPaymentCommand.full( // 스케쥴러에 의한 취소는 완료되지 못한 결제에 대한 전액 취소이므로
						payment.getId(),
						SYSTEM_REQUESTER_ID,
						TIMEOUT_REASON
					);
					cancelPaymentUseCase.cancel(command);
					totalCanceled++;
				} catch (Exception e) {
					log.error("[Payment] 결제 자동 취소 실패. paymentId={}", payment.getId(), e);
				}
			}
		} while (slice.hasNext());

		if (totalCanceled > 0) {
			log.info("[Payment] 만료된 결제 {}건 자동 취소 완료. threshold={}", totalCanceled,
				threshold);
		}
	}

}
