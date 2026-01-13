package domain.wallet;

import java.time.LocalDateTime;

import domain.member.MoneyMember;
import vo.Money;

public class Wallet {
	private Long id;
	private MoneyMember member;
	private Money balance;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	public Wallet() {
	}

	public Wallet(Long id, MoneyMember member, Money balance, LocalDateTime createdAt, LocalDateTime modifiedAt) {
		this.id = id;
		this.member = member;
		this.balance = balance;
		this.createdAt = createdAt;
		this.modifiedAt = modifiedAt;
	}

	public Wallet(MoneyMember member) {
		this(null, member, Money.zero(), null, null);
	}

	public Long getId() {
		return id;
	}

	public MoneyMember getMember() {
		return member;
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
