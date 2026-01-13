package domain.wallet;

import java.time.LocalDateTime;

import vo.Money;

public class Wallet {
	private Long walletId;
	private Long userId;
	private Money balance;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
