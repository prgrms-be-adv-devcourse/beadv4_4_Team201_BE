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
        validateNull(amount);
        if (amount.isLessThan(Money.of(1000))) {
            throw new WalletException(WalletErrorCode.WALLET_CHARGE_AMOUNT_BELOW_MINIMUM);
        }

		balance = balance.plus(amount);
	}

    public void withdraw(Money amount) {
        validateNull(amount);
        if (balance.isLessThan(amount)) {
            throw new WalletException(WalletErrorCode.INSUFFICIENT_BALANCE);
        }

        balance = balance.minus(amount);
    }

    private static void validateNull(Money amount) {
        if (amount == null) {
            throw new WalletException(WalletErrorCode.INVALID_NULL_AMOUNT);
        }
    }
}
