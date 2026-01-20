package domain.wallet;

import app.giftify.shared.domain.vo.Money;

public interface WalletWithdrawUseCase {
    void withdraw(
            Long memberId,
            Money amount,
            String transactionType,
            String referenceType,
            Long referenceId
    );
}
