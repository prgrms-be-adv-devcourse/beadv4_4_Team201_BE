package app.giftify.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.CreatePaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentCreateContext;
import app.giftify.payment.domain.PaymentMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CreatePaymentService implements CreatePaymentUseCase {
	private final PaymentRepository paymentRepository;

	public CreatePaymentService(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Override
	public PaymentCreatedResult create(CreatePaymentCommand command) {
		// 1. 멱등성 체크 - 이미 존재하는 결제인지 확인
		return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
			.map(existing -> new PaymentCreatedResult(
				existing.getId(),
				existing.getIdempotencyKey(),
				existing.getStatus(),
				requiresPgApproval(existing.getMethod())
			))
			.orElseGet(() -> createNewPayment(command));
	}

	private PaymentCreatedResult createNewPayment(CreatePaymentCommand command) {
		// 2. PaymentCreateContext 생성
		PaymentCreateContext context = new PaymentCreateContext(
			command.memberId(),
			command.orderId(),
			command.type(),
			command.method()
		);

		// 3. Payment 생성
		Payment payment = Payment.create(
			context,
			command.idempotencyKey(),
			command.expectedAmount(),
			command.expectedAmount(),  // paidAmount = originAmount (초기값)
			command.orderItems()
		);

		// 4. 저장
		Payment savedPayment = paymentRepository.save(payment);

		// 5. 결과 반환
		return new PaymentCreatedResult(
			savedPayment.getId(),
			savedPayment.getIdempotencyKey(),
			savedPayment.getStatus(),
			requiresPgApproval(savedPayment.getMethod())
		);
	}

	private boolean requiresPgApproval(PaymentMethod method) {
		// WALLET 결제는 PG 승인 불필요
		return method != PaymentMethod.WALLET;
	}
}
