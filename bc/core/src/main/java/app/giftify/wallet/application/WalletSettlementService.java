package app.giftify.wallet.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.wallet.application.inbound.SettlementPayoutCommand;
import app.giftify.wallet.application.inbound.SettlementPayoutUseCase;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletSettlementService implements SettlementPayoutUseCase {

	private final WalletRepository walletRepository;
	private final WalletHistoryRepository historyRepository;

	@Override
	@Transactional
	public void payout(SettlementPayoutCommand command) {
		if (historyRepository
			.existsByReferenceIdAndReferenceType(command.referenceId(), ReferenceType.SETTLEMENT)
		) {
			log.info("[WalletSettlementService] 중복 정산 지급 무시. settlementId={}", command.settlementId());
			return;
		}

		Wallet wallet = resolveWallet(command);

		if (command.amount().isPositive()) {
			wallet.charge(command.amount());
		} else {
			wallet.deductForPayment(command.amount().abs());
		}

		walletRepository.save(wallet);

		historyRepository.recordTransaction(
			wallet.getId(),
			command.amount().isPositive() ? TransactionType.SETTLEMENT_PAYOUT : TransactionType.SETTLEMENT_CLAWBACK, // 정산 지급/차감 분기
			command.amount().abs(),
			wallet.getBalance(),
			ReferenceType.SETTLEMENT,
			command.referenceId()
		);

		log.info("[WalletSettlementService] 정산 {} 완료. sellerId={}, amount={}, balance={}",
			command.amount().isPositive() ? TransactionType.SETTLEMENT_PAYOUT : TransactionType.SETTLEMENT_CLAWBACK,
			command.sellerId(), command.amount(), wallet.getBalance());
	}

	private Wallet resolveWallet(SettlementPayoutCommand command) {
		return walletRepository.findByMemberId(command.sellerId())
			.orElseThrow(() -> new WalletException(
				WalletErrorCode.WALLET_NOT_FOUND,
				"[WalletSettlementService] 지갑 미존재. sellerId=" + command.sellerId()
			));
	}
}
