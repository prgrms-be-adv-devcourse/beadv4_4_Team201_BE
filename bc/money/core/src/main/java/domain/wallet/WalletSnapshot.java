package domain.wallet;

import vo.Money;

import java.time.LocalDateTime;

public record WalletSnapshot(
    Long id,
    Long memberId,
    Money balance,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {}