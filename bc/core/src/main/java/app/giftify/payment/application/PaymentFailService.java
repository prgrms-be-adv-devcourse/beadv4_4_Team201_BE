package app.giftify.payment.application;

import app.giftify.payment.application.inbound.FailPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentFailService implements FailPaymentUseCase {

	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

	@Override
	@Transactional
	public void fail(Payment payment) {
		Payment failed = payment.fail(LocalDateTime.now());
		var domainEvents = failed.pullEvents();
		paymentRepository.save(failed);
		domainEvents.forEach(eventPublisher::publish);
	}
}
