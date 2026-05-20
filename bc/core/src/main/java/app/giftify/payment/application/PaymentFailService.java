package app.giftify.payment.application;

import app.giftify.payment.application.inbound.FailPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentFailService implements FailPaymentUseCase {

	private final PaymentRepository paymentRepository;
	private final PaymentModuleEventPublisher moduleEventPublisher;

	@Override
	@Transactional
	public void fail(Payment payment) {
		Payment failed = payment.fail();
		paymentRepository.save(failed);
		moduleEventPublisher.publishFrom(failed, payment);
	}
}
