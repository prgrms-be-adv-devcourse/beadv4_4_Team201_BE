package app.giftify.payment.adapter.in.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import domain.payment.CancelReason;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.command.CancelPaymentCommand;

@Component
public class PaymentTimeoutScheduler {
	private static final Logger log = LoggerFactory.getLogger(PaymentTimeoutScheduler.class);

	private final PaymentCancelUseCase paymentCancelUseCase;
	private final PaymentRepository paymentRepository;

	public PaymentTimeoutScheduler(PaymentCancelUseCase paymentCancelUseCase, PaymentRepository paymentRepository) {
		this.paymentCancelUseCase = paymentCancelUseCase;
		this.paymentRepository = paymentRepository;
	}

	@Value("${payment.timeout.minutes:30}")
	private int timeoutMinutes;

	@Scheduled(fixedDelayString = "${payment.timeout.check-interval:60000}") // 1분마다 실행 == 기본값
	public void cancelExpiredPayments() {
		LocalDateTime threshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
		List<Payment> expiredPayments = paymentRepository.findPendingPaymentsBefore(threshold);

		if (expiredPayments.isEmpty()) {
			log.info("[Payment] 일정 시간이 지나 만료된 결제가 없습니다. (기준 시간: {})", threshold);
			return;
		}

		log.info("[Payment] 일정 시간이 지나 만료된 결제 {}건에 대해 자동 취소를 진행합니다. (기준 시간: {})",
			expiredPayments.size(), threshold);

		for (Payment payment : expiredPayments) {
			try {
				paymentCancelUseCase.cancel(new CancelPaymentCommand(
					payment.getPaymentId(),
					null, // 시스템 자동 취소, 사용자 요청이 아님
					CancelReason.TIMEOUT
				));
			} catch (Exception e) {
				// 개별 취소 실패가 전체 스케줄러 실행에 영향을 주지 않도록 하기 위해 로그만 남기기
				log.error("[Payment] 결제 자동 취소 중 오류 발생. paymentId={}", payment.getPaymentId(), e);
			}
		}
	}
}
