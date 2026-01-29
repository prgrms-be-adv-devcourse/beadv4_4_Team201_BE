package app.giftify.wallet.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

public class Wallet extends BaseDomainModel {
	private static final Money MINIMUM_CHARGE_AMOUNT = Money.of(1000);

	private final Long memberId;
	private Money balance;

	private Wallet(Long id, Long memberId, Money balance) {
		super(id);
		this.memberId = memberId;
		this.balance = balance;
	}

	/**
	 * 새로운 지갑을 생성합니다.
	 *
	 * @param memberId 회원 ID
	 * @param balance  초기 잔액
	 * @return 생성된 Wallet 객체
	 */
	public static Wallet create(Long memberId, Money balance) {
		validateNull(memberId, "memberId");
		validateNull(balance, "balance");
		return new Wallet(null, memberId, balance);
	}

	/**
	 * 스냅샷으로부터 지갑을 복원합니다.
	 *
	 * @param snapshot 지갑 스냅샷
	 * @return 복원된 Wallet 객체
	 */
	public static Wallet restore(WalletSnapshot snapshot) {
		validateNull(snapshot, "snapshot");
		return new Wallet(snapshot.id(), snapshot.memberId(), snapshot.balance());
	}

	/**
	 * 현재 상태를 스냅샷으로 반환합니다.
	 *
	 * @return 지갑 스냅샷
	 */
	public WalletSnapshot snapshot() {
		return new WalletSnapshot(getId(), memberId, balance);
	}

	/**
	 * 지갑에 금액을 충전합니다.
	 * 최소 충전 금액은 1000원입니다.
	 *
	 * @param amount 충전할 금액
	 * @throws WalletException 충전 금액이 null이거나 최소 금액 미만인 경우
	 */
	public void charge(Money amount) {
		validateNull(amount, "amount");
		if (amount.isLessThan(MINIMUM_CHARGE_AMOUNT)) {
			throw new WalletException(WalletErrorCode.CHARGE_AMOUNT_BELOW_MINIMUM);
		}
		balance = balance.plus(amount);
	}

	/**
	 * 지갑에서 금액을 출금합니다.
	 *
	 * @param amount 출금할 금액
	 * @throws WalletException 출금 금액이 null이거나 잔액이 부족한 경우
	 */
	public void withdraw(Money amount) {
		validateNull(amount, "amount");
		if (balance.isLessThan(amount)) {
			throw new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE);
		}
		balance = balance.minus(amount);
	}

	/**
	 * 결제를 위해 지갑에서 금액을 차감합니다.
	 *
	 * @param amount 차감할 금액
	 * @throws WalletException 차감 금액이 null이거나 잔액이 부족한 경우
	 */
	public void deductForPayment(Money amount) {
		validateNull(amount, "amount");
		if (balance.isLessThan(amount)) {
			throw new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE);
		}
		balance = balance.minus(amount);
	}

	public Long getMemberId() {
		return memberId;
	}

	public Money getBalance() {
		return balance;
	}

	private static void validateNull(Object value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException("[Wallet] " + fieldName + "은(는) null일 수 없습니다.");
		}
	}
}
