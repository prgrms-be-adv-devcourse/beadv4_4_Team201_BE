package domain.wallet;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class Wallet extends BaseDomainModel {
    private final Long memberId;
	private Money balance;

    public Wallet(Long memberId, Money balance) {
        this(null, memberId, balance, null, null);
    }

    private Wallet(Long id, Long memberId, Money balance, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super(id, createdAt, updatedAt);
        this.memberId = memberId;
		this.balance = balance;
	}

    public static Wallet create(Long memberId, Money balance) {
        return new Wallet(null, memberId, balance, null, null);
    }

    public static Wallet restore(WalletSnapshot snapshot) {
        return new Wallet(
                snapshot.id(),
                snapshot.memberId(),
                snapshot.balance(),
                snapshot.createdAt(),
                snapshot.updatedAt()
        );
    }

    public WalletSnapshot snapshot() {
        return new WalletSnapshot(
                super.getId(),
                memberId,
                balance,
                super.getCreatedAt(),
				super.getUpdatedAt()
        );
	}

	public void charge(Money amount) {
        validateCharge(amount);

		balance = balance.plus(amount);
	}

    public Long getMemberId() {
        return memberId;
	}

	public Money getBalance() {
		return balance;
	}

    private void validateCharge(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("충전 금액은 null일 수 없습니다.");
        }
        if (amount.equals(Money.zero())) {
            throw new IllegalArgumentException("충전 금액은 최소 1000원이어야 합니다.");
        }
    }
}
