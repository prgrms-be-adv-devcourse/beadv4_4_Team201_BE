package app.giftify.payment.application;

import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.ChargeDepositCommand;
import app.giftify.payment.application.inbound.ChargeDepositUseCase;
import app.giftify.payment.application.inbound.CreateFundingPaymentCommand;
import app.giftify.payment.application.inbound.CreateFundingPaymentUseCase;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentCreateContext;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class CreatePaymentService implements ChargeDepositUseCase, CreateFundingPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final DeductWalletUseCase deductWalletUseCase;

	public CreatePaymentService(PaymentRepository paymentRepository, DeductWalletUseCase deductWalletUseCase) {
		this.paymentRepository = paymentRepository;
		this.deductWalletUseCase = deductWalletUseCase;
	}

	//--------- ChargeDepositUseCase 구현 - 예치금 충전 ----------//

	@Override
	public PaymentCreatedResult charge(ChargeDepositCommand command) {
		// 1. 멱등성 체크 - orderId로 이미 존재하는 결제인지 확인
		return paymentRepository.findByIdempotencyKey(command.orderId())
			.map(existing -> new PaymentCreatedResult(
				existing.getId(),
				existing.getOrderId(),
				existing.getStatus(),
				existing.getPaymentKey(),
				null,
				existing.getCreatedAt()
			))
			.orElseGet(() -> createDepositChargePayment(command));
	}

	private PaymentCreatedResult createDepositChargePayment(ChargeDepositCommand command) {
		// PaymentCreateContext 생성 - 예치금 충전은 항상 DEPOSIT_CHARGE + CARD
		PaymentCreateContext context = new PaymentCreateContext(
			command.memberId(),
			command.orderId(),
			PaymentType.DEPOSIT_CHARGE,
			PaymentMethod.CARD
		);

		// Payment 생성 - orderItems 없음
		Payment payment = Payment.create(
			context,
			command.orderId(),
			command.amount(),
			command.amount(),
			Collections.emptyList()
		);

		Payment savedPayment = paymentRepository.save(payment);

		log.info("[Payment] 예치금 충전 결제 생성. paymentId={}, orderId={}, amount={}",
			savedPayment.getId(), savedPayment.getOrderId(), command.amount());

		return new PaymentCreatedResult(
			savedPayment.getId(),
			savedPayment.getOrderId(),
			savedPayment.getStatus(),
			savedPayment.getPaymentKey(),
			null,
			savedPayment.getCreatedAt()
		);
	}

	//--------- CreateFundingPaymentUseCase 구현 - 펀딩 결제 ----------//

	@Override
	public PaymentCreatedResult create(CreateFundingPaymentCommand command) {
		// 1. 멱등성 체크 - orderId로 이미 존재하는 결제인지 확인
		return paymentRepository.findByIdempotencyKey(command.orderId())
			.map(existing -> new PaymentCreatedResult(
				existing.getId(),
				existing.getOrderId(),
				existing.getStatus(),
				existing.getPaymentKey(),
				null,
				existing.getCreatedAt()
			))
			.orElseGet(() -> createFundingPayment(command));
	}

	private PaymentCreatedResult createFundingPayment(CreateFundingPaymentCommand command) {
		// PaymentCreateContext 생성
		PaymentCreateContext context = new PaymentCreateContext(
			command.memberId(),
			command.orderId(),
			command.getType(),  // 항상 FUNDING -> 내부에 메서드 미리 준비해놓음
			command.method()
		);

		// Payment 생성
		Payment payment = Payment.create(
			context,
			command.orderId(),
			command.expectedAmount(),
			command.expectedAmount(),
			command.orderItems()
		);

		Payment savedPayment = paymentRepository.save(payment);

		// 내부 지갑 결제(예치금)면 즉시 처리
		if (command.method().isWalletPayment()) {
			return handleWalletPaymentForFunding(savedPayment, command);
		}

		log.info("[Payment] 펀딩 결제 생성. paymentId={}, orderId={}, amount={}",
			savedPayment.getId(), savedPayment.getOrderId(), command.expectedAmount());

		return new PaymentCreatedResult(
			savedPayment.getId(),
			savedPayment.getOrderId(),
			savedPayment.getStatus(),
			savedPayment.getPaymentKey(),
			null,
			savedPayment.getCreatedAt()
		);
	}

	private PaymentCreatedResult handleWalletPaymentForFunding(Payment payment, CreateFundingPaymentCommand command) {
		DeductWalletCommand deductCommand = new DeductWalletCommand(
			command.memberId(),
			payment.getId(),
			command.orderId(),
			command.expectedAmount()
		);

		DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

		if (!result.success()) {
			log.warn("[Payment] 예치금 잔액 부족. paymentId={}, required={}, current={}",
				payment.getId(), result.requiredAmount(), result.currentBalance());

			throw new PaymentException(
				PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
				String.format("필요 금액: %s, 현재 잔액: %s",
					result.requiredAmount(), result.currentBalance())
			);
		}

		log.info("[Payment] 예치금 결제 요청 완료. paymentId={}, walletId={}",
			payment.getId(), result.walletId());

		return new PaymentCreatedResult(
			payment.getId(),
			payment.getOrderId(),
			PaymentStatus.PENDING,
			payment.getPaymentKey(),
			null,
			payment.getCreatedAt()
		);
	}
}