package payment.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentCreateContext;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.wallet.Wallet;
import payment.usecase.PaymentInitiateUseCase;
import payment.usecase.command.PaymentInitiateCommand;
import payment.usecase.result.PaymentInitiateResult;
import wallet.service.WalletService;

/**
 * 결제 시작 서비스.
 * 현재는 예치금 전용 결제만 지원합니다.
 * 복합 결제(예치금 + PG)는 향후 활성화 예정입니다.
 */
@Service
@Transactional
public class PaymentInitiateService implements PaymentInitiateUseCase {

	private static final Logger log = LoggerFactory.getLogger(PaymentInitiateService.class);
	private static final String TRANSACTION_TYPE_PAYMENT = "PAYMENT_INITIATE";
	private static final String REFERENCE_TYPE_PAYMENT = "PAYMENT";
	private static final String REFERENCE_TYPE_ROLLBACK = "PAYMENT_ROLLBACK";

	private final WalletService walletService;
	private final PaymentRepository paymentRepository;
	private final List<PaymentPolicy> policies;

	// 임시 참조 ID 생성 (실제로는 Order ID 등을 사용해야 함)
	private final AtomicLong referenceIdGenerator = new AtomicLong(System.currentTimeMillis());

	public PaymentInitiateService(
		WalletService walletService,
		PaymentRepository paymentRepository,
		List<PaymentPolicy> policies
	) {
		this.walletService = walletService;
		this.paymentRepository = paymentRepository;
		this.policies = policies;
	}

	/**
	 * 결제 시작 (현재: 예치금 전용)
	 * 예치금 잔액이 충분하면 예치금에서 차감하고, 부족하면 예외를 발생시킵니다.
	 */
	@Override
	public PaymentInitiateResult initiate(PaymentInitiateCommand command) {
		log.info("[PaymentInitiate] 결제 시작. userId={}, orderId={}, amount={}, type={}",
			command.userId(), command.orderId(), command.amount(), command.paymentType());

		// 1. 정책 검증 (PaymentType에 따라 다른 정책 적용)
		validatePolicy(command);

		// 2. 예치금 잔액 확인
		Wallet wallet = walletService.getWalletByMemberId(command.userId());
		Money walletBalance = wallet.getBalance();
		Money requestAmount = command.amount();

		log.debug("[PaymentInitiate] 예치금 잔액 확인. userId={}, balance={}, requestAmount={}",
			command.userId(), walletBalance, requestAmount);

		// 3. 예치금으로 완납 가능한 경우
		if (walletBalance.isGreaterThanOrEqual(requestAmount)) {
			return payWithWalletOnly(command.userId(), command.orderId(), requestAmount);
		}

		// 4. 예치금 부족 - 현재는 예치금 전용 결제만 지원
		log.warn("[PaymentInitiate] 예치금 부족. userId={}, balance={}, requestAmount={}",
			command.userId(), walletBalance, requestAmount);
		throw new PaymentException(PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
			String.format("예치금 잔액이 부족합니다. 잔액: %s, 요청 금액: %s", walletBalance, requestAmount));
	}

	/**
	 * 예치금 전용 결제 처리
	 */
	private PaymentInitiateResult payWithWalletOnly(Long userId, Long orderId, Money amount) {
		walletService.withdraw(
			userId,
			amount,
			TRANSACTION_TYPE_PAYMENT,
			REFERENCE_TYPE_PAYMENT,
			orderId != null ? orderId : generateReferenceId()
		);

		log.info("[PaymentInitiate] 예치금으로 완납. userId={}, orderId={}, walletUsed={}",
			userId, orderId, amount);
		return PaymentInitiateResult.completedWithWallet(orderId, amount);
	}

	/**
	 * 복합 결제 처리 (예치금 + PG)
	 * 현재 미사용. 향후 복합 결제 활성화 시 initiate()에서 호출.
	 *
	 * @param userId 사용자 ID
	 * @param orderId Order BC의 주문 ID
	 * @param walletBalance 예치금 잔액 (전액 차감됨)
	 * @param requestAmount 요청 금액
	 * @param paymentType 결제 타입
	 * @return 복합 결제 결과 (PG 결제 정보 포함)
	 */
	@SuppressWarnings("unused")
	private PaymentInitiateResult payWithWalletAndPg(
		Long userId,
		Long orderId,
		Money walletBalance,
		Money requestAmount,
		PaymentType paymentType
	) {
		Money walletUsed = walletBalance;
		Money pgRequired = requestAmount.minus(walletBalance);

		// 예치금 전액 차감 (잔액이 0보다 큰 경우)
		if (walletUsed.isGreaterThan(Money.zero())) {
			walletService.withdraw(
				userId,
				walletUsed,
				TRANSACTION_TYPE_PAYMENT,
				REFERENCE_TYPE_PAYMENT,
				orderId != null ? orderId : generateReferenceId()
			);

			log.info("[PaymentInitiate] 예치금 차감 완료. userId={}, walletUsed={}", userId, walletUsed);
		}

		// PG 결제용 Payment 생성 (walletUsedAmount 포함)
		Payment payment = Payment.createForFunding(userId, pgRequired, walletUsed);
		var savedPayment = paymentRepository.save(payment);

		log.info("[PaymentInitiate] PG 결제 필요. userId={}, orderId={}, walletUsed={}, pgRequired={}, paymentId={}",
			userId, orderId, walletUsed, pgRequired, savedPayment.getPaymentId());

		return PaymentInitiateResult.requiresPgPayment(
			orderId,
			walletUsed,
			pgRequired,
			savedPayment.getPaymentId(),
			savedPayment.getOrderId()
		);
	}

	@Override
	public void rollbackWallet(Long userId, Money amount, Long paymentId) {
		if (amount == null || !amount.isGreaterThan(Money.zero())) {
			log.debug("[PaymentInitiate] 롤백할 예치금 없음. userId={}, paymentId={}", userId, paymentId);
			return;
		}

		log.info("[PaymentInitiate] 예치금 롤백 시작. userId={}, amount={}, paymentId={}",
			userId, amount, paymentId);

		walletService.charge(
			userId,
			amount,
			"PAYMENT_ROLLBACK",
			REFERENCE_TYPE_ROLLBACK,
			paymentId
		);

		log.info("[PaymentInitiate] 예치금 롤백 완료. userId={}, amount={}, paymentId={}",
			userId, amount, paymentId);
	}

	private void validatePolicy(PaymentInitiateCommand command) {
		PaymentType type = command.paymentType();

		PaymentPolicy policy = policies.stream()
			.filter(p -> p.support(type))
			.findFirst()
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.UNSUPPORTED_PAYMENT_TYPE,
				"[PaymentInitiate] 지원하지 않는 결제 타입입니다: " + type));

		PaymentCreateContext context = new PaymentCreateContext(
			command.userId(),
			command.amount(),
			type
		);

		policy.validate(context);
	}

	private Long generateReferenceId() {
		return referenceIdGenerator.incrementAndGet();
	}
}