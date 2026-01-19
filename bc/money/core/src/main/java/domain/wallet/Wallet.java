package domain.wallet;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;
import domain.errorCode.WalletErrorCode;
import domain.exception.WalletException;

public class Wallet extends BaseDomainModel {
    private final Long memberId;
	private Money balance;

    public Wallet(Long memberId, Money balance) {
        this(null, memberId, balance);
    }

    private Wallet(Long id, Long memberId, Money balance) {
		super(id);
        this.memberId = memberId;
		this.balance = balance;
	}

    public Long getMemberId() {
        return memberId;
    }

    public Money getBalance() {
        return balance;
    }

    public static Wallet create(Long memberId, Money balance) {
        return new Wallet(null, memberId, balance);
    }

    public static Wallet restore(WalletSnapshot snapshot) {
        return new Wallet(
                snapshot.id(),
                snapshot.memberId(),
                snapshot.balance()
        );
    }

    public WalletSnapshot snapshot() {
        return new WalletSnapshot(
                super.getId(),
                memberId,
                balance
        );
	}

	public void charge(Money amount) {
        validateCharge(amount);

		balance = balance.plus(amount);
	}

    public void withdraw(Money amount) {
        validateWithdraw(amount);

        balance = balance.minus(amount);
    }

    private void validateCharge(Money amount) {
        // todo: 커스텀 예외 적용
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 null일 수 없습니다.");
        }
        if (amount.equals(Money.zero())) {
            throw new IllegalArgumentException("충전 금액은 최소 1000원이어야 합니다.");
        }
    }

    private void validateWithdraw(Money amount) {
        if (amount == null) {
            throw new WalletException(WalletErrorCode.INVALID_NULL_AMOUNT);
        }
        if (balance.isLessThan(amount)) {
            throw new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE);
        }
    }
}
