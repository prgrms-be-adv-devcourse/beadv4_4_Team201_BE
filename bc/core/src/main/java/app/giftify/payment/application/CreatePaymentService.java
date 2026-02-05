package app.giftify.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.CreatePaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentCreateContext;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CreatePaymentService implements CreatePaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final DeductWalletUseCase deductWalletUseCase;

	public CreatePaymentService(PaymentRepository paymentRepository, DeductWalletUseCase deductWalletUseCase) {
		this.paymentRepository = paymentRepository;
		this.deductWalletUseCase = deductWalletUseCase;
	}

	@Override
	public PaymentCreatedResult create(CreatePaymentCommand command) {
		// 1. 멱등성 체크 - 이미 존재하는 결제인지 확인
		return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
			.map(existing -> new PaymentCreatedResult(
				existing.getId(),
				existing.getOrderId(),
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

		// 5. 내부 지갑 결제(예치금)면 즉시 처리
		if (command.method().isWalletPayment()) {
			return handleWalletPayment(savedPayment, command);
		}

		// 6. PG 결제는 기존 플로우
		return new PaymentCreatedResult(
			savedPayment.getId(),
			savedPayment.getOrderId(),
			savedPayment.getIdempotencyKey(),
			savedPayment.getStatus(),
			true
		);
	}

	private boolean requiresPgApproval(PaymentMethod method) {
		// 내부 지갑 결제(예치금/포인트)는 PG 승인 불필요
		return !method.isWalletPayment();
	}

	private PaymentCreatedResult handleWalletPayment(Payment payment, CreatePaymentCommand command) {
		DeductWalletCommand deductCommand = new DeductWalletCommand(
			command.memberId(),
			payment.getId(),
			command.orderId(),
			command.expectedAmount()
		);

		DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

		if (!result.success()) {
			log.warn("[Payment] 지갑 잔액 부족. paymentId={}, required={}, current={}",
				payment.getId(), result.requiredAmount(), result.currentBalance());

			return PaymentCreatedResult.insufficientWalletBalance(
				payment.getId(),
				payment.getOrderId(),
				command.idempotencyKey(),
				result.requiredAmount(),
				result.currentBalance()
			);
		}

		log.info("[Payment] WALLET 결제 요청 완료. paymentId={}, walletId={}",
			payment.getId(), result.walletId());

		return new PaymentCreatedResult(
			payment.getId(),
			payment.getOrderId(),
			payment.getIdempotencyKey(),
			PaymentStatus.PENDING,
			false
		);
	}
}
