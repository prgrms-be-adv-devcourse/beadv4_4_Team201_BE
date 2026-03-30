package app.giftify.payment.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.FailPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentFailService implements FailPaymentUseCase {

	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

	@Override
	@Transactional
	public void fail(Payment payment) {
		payment.markAsFailed(LocalDateTime.now());
		var domainEvents = payment.pullEvents();
		paymentRepository.save(payment);
		domainEvents.forEach(eventPublisher::publish);
	}
}
