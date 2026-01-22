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
import payment.usecase.FundingPaymentUseCase;
import payment.usecase.command.FundingContributeCommand;
import payment.usecase.result.FundingContributeResult;
import wallet.service.WalletService;

/**
 * 펀딩 참여 결제 서비스.
 * 현재는 예치금 전용 결제만 지원합니다.
 * 복합 결제(예치금 + PG)는 향후 활성화 예정입니다.
 */
@Service
@Transactional
public class FundingPaymentService implements FundingPaymentUseCase {

	private static final Logger log = LoggerFactory.getLogger(FundingPaymentService.class);
	private static final String TRANSACTION_TYPE_FUNDING = "FUNDING_CONTRIBUTE";
	private static final String REFERENCE_TYPE_FUNDING = "FUNDING";
	private static final String REFERENCE_TYPE_ROLLBACK = "FUNDING_ROLLBACK";

	private final WalletService walletService;
	private final PaymentRepository paymentRepository;
	private final List<PaymentPolicy> policies;

	// 임시 참조 ID 생성 (실제로는 펀딩 ID 등을 사용해야 함)
	private final AtomicLong referenceIdGenerator = new AtomicLong(System.currentTimeMillis());

	public FundingPaymentService(
		WalletService walletService,
		PaymentRepository paymentRepository,
		List<PaymentPolicy> policies
	) {
		this.walletService = walletService;
		this.paymentRepository = paymentRepository;
		this.policies = policies;
	}

	/**
	 * 펀딩 참여 결제 (현재: 예치금 전용)
	 * 예치금 잔액이 충분하면 예치금에서 차감하고, 부족하면 예외를 발생시킵니다.
	 */
	@Override
	public FundingContributeResult contribute(FundingContributeCommand command) {
		log.info("[FundingPayment] 펀딩 참여 시작. userId={}, amount={}",
			command.userId(), command.amount());

		// 1. 정책 검증 (최소 1,000원)
		validateFundingPolicy(command);

		// 2. 예치금 잔액 확인
		Wallet wallet = walletService.getWalletByMemberId(command.userId());
		Money walletBalance = wallet.getBalance();
		Money requestAmount = command.amount();

		log.debug("[FundingPayment] 예치금 잔액 확인. userId={}, balance={}, requestAmount={}",
			command.userId(), walletBalance, requestAmount);

		// 3. 예치금으로 완납 가능한 경우
		if (walletBalance.isGreaterThanOrEqual(requestAmount)) {
			return payWithWalletOnly(command.userId(), requestAmount);
		}

		// 4. 예치금 부족 - 현재는 예치금 전용 결제만 지원
		log.warn("[FundingPayment] 예치금 부족. userId={}, balance={}, requestAmount={}",
			command.userId(), walletBalance, requestAmount);
		throw new PaymentException(PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
			String.format("예치금 잔액이 부족합니다. 잔액: %s, 요청 금액: %s", walletBalance, requestAmount));
	}

	/**
	 * 예치금 전용 결제 처리
	 */
	private FundingContributeResult payWithWalletOnly(Long userId, Money amount) {
		walletService.withdraw(
			userId,
			amount,
			TRANSACTION_TYPE_FUNDING,
			REFERENCE_TYPE_FUNDING,
			generateReferenceId()
		);

		log.info("[FundingPayment] 예치금으로 완납. userId={}, walletUsed={}", userId, amount);
		return FundingContributeResult.completedWithWallet(amount);
	}

	/**
	 * 복합 결제 처리 (예치금 + PG)
	 * 현재 미사용. 향후 복합 결제 활성화 시 contribute()에서 호출.
	 *
	 * @param userId 사용자 ID
	 * @param walletBalance 예치금 잔액 (전액 차감됨)
	 * @param requestAmount 요청 금액
	 * @return 복합 결제 결과 (PG 결제 정보 포함)
	 */
	@SuppressWarnings("unused")
	private FundingContributeResult payWithWalletAndPg(
		Long userId,
		Money walletBalance,
		Money requestAmount
	) {
		Money walletUsed = walletBalance;
		Money pgRequired = requestAmount.minus(walletBalance);

		// 예치금 전액 차감 (잔액이 0보다 큰 경우)
		if (walletUsed.isGreaterThan(Money.zero())) {
			walletService.withdraw(
				userId,
				walletUsed,
				TRANSACTION_TYPE_FUNDING,
				REFERENCE_TYPE_FUNDING,
				generateReferenceId()
			);

			log.info("[FundingPayment] 예치금 차감 완료. userId={}, walletUsed={}", userId, walletUsed);
		}

		// PG 결제용 Payment 생성 (walletUsedAmount 포함)
		Payment payment = Payment.createForFunding(userId, pgRequired, walletUsed);
		var savedPayment = paymentRepository.save(payment);

		log.info("[FundingPayment] PG 결제 필요. userId={}, walletUsed={}, pgRequired={}, paymentId={}",
			userId, walletUsed, pgRequired, savedPayment.getPaymentId());

		return FundingContributeResult.requiresPgPayment(
			walletUsed,
			pgRequired,
			savedPayment.getPaymentId(),
			savedPayment.getOrderId()
		);
	}

	@Override
	public void rollbackWallet(Long userId, Money amount, Long paymentId) {
		if (amount == null || !amount.isGreaterThan(Money.zero())) {
			log.debug("[FundingPayment] 롤백할 예치금 없음. userId={}, paymentId={}", userId, paymentId);
			return;
		}

		log.info("[FundingPayment] 예치금 롤백 시작. userId={}, amount={}, paymentId={}",
			userId, amount, paymentId);

		walletService.charge(
			userId,
			amount,
			"FUNDING_ROLLBACK",
			REFERENCE_TYPE_ROLLBACK,
			paymentId
		);

		log.info("[FundingPayment] 예치금 롤백 완료. userId={}, amount={}, paymentId={}",
			userId, amount, paymentId);
	}

	private void validateFundingPolicy(FundingContributeCommand command) {
		PaymentType type = PaymentType.FUNDING;

		PaymentPolicy policy = policies.stream()
			.filter(p -> p.support(type))
			.findFirst()
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.UNSUPPORTED_PAYMENT_TYPE,
				"[FundingPayment] 지원하지 않는 결제 타입입니다: " + type));

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