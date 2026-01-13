package wallet.usecase;

import domain.member.MoneyMember;
import domain.wallet.Wallet;

public interface WalletCreateUseCase {

	Wallet createWallet(MoneyMember member);
}
