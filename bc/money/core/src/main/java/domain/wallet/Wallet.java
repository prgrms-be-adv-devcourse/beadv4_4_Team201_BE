package domain.wallet;

import vo.Money;

import java.time.LocalDateTime;

public class Wallet {
	private Long id;
    private Long memberId;
	private Money balance;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Wallet() {
	}

    public Wallet(Long memberId, Money balance) {
        this(null, memberId, balance, null, null);
    }

    private Wallet(Long id, Long memberId, Money balance, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.id = id;
        this.memberId = memberId;
		this.balance = balance;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
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
                snapshot.modifiedAt()
        );
    }

    public WalletSnapshot snapshot() {
        return new WalletSnapshot(
                id,
                memberId,
                balance,
                createdAt,
                modifiedAt
        );
	}

	public Long getId() {
		return id;
	}

    public Long getMemberId() {
        return memberId;
	}

	public Money getBalance() {
		return balance;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getModifiedAt() {
		return modifiedAt;
	}
}
