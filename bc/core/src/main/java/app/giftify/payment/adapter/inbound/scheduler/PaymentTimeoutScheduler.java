package app.giftify.payment.adapter.inbound.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.inbound.FailPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static app.giftify.payment.domain.SystemConstants.SYSTEM_REQUESTER_ID;

@Component
@RequiredArgsConstructor
public class PaymentTimeoutScheduler {
	private static final Logger log = LoggerFactory.getLogger(PaymentTimeoutScheduler.class);


    private static final String TIMEOUT_REASON = "TIMEOUT";
    private static final int BATCH_SIZE = 100;

    private final CancelPaymentUseCase cancelPaymentUseCase;
    private final FailPaymentUseCase failPaymentUseCase;
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
                    if (payment.getWalletDeductedAmount().isGreaterThan(Money.zero())) {
                        failPaymentUseCase.fail(payment);
                    } else {
                        CancelPaymentCommand command = CancelPaymentCommand.full(
                                payment.getId(),
                                SYSTEM_REQUESTER_ID,
                                TIMEOUT_REASON
                        );
                        cancelPaymentUseCase.cancel(command);
                    }
                    totalCanceled++;
                } catch (Exception e) {
                    log.error("[Payment] 결제 자동 처리 실패. paymentId={}", payment.getId(), e);
                }
            }
        } while (slice.hasNext());

        if (totalCanceled > 0) {
            log.info("[Payment] 만료된 결제 {}건 자동 처리 완료. threshold={}", totalCanceled,
                    threshold);
        }
    }

}
