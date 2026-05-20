package app.giftify.wallet.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import app.giftify.wallet.application.outbound.WalletHistoryRepository;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import app.giftify.wallet.domain.event.WalletDeductedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletPaymentService implements DeductWalletUseCase {
	private static final Logger log = LoggerFactory.getLogger(WalletPaymentService.class);


	private final WalletRepository walletRepository;
	private final WalletHistoryRepository historyRepository;
	private final EventPublisher eventPublisher;

	@Transactional
	@Override
	public DeductWalletResult deductForPayment(DeductWalletCommand command) {
		if (historyRepository.existsByReferenceIdAndReferenceType(
			command.orderId(), ReferenceType.PAYMENT)) {
			throw new WalletException(WalletErrorCode.DUPLICATED_TRANSACTION);
		}

		Wallet wallet = walletRepository.findByMemberId(command.memberId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));

		try {
			wallet.deductForPayment(command.amount());
		} catch (WalletException e) {
			if (e.getErrorCode() == WalletErrorCode.INSUFFICIENT_BALANCE) {
				log.warn("[Wallet] 잔액 부족. memberId={}, required={}, current={}",
					command.memberId(), command.amount(), wallet.getBalance());
				return DeductWalletResult.insufficientBalance(
					wallet.getId(),
					command.amount(),
					wallet.getBalance()
				);
			}
			throw e;
		}

		Wallet savedWallet = walletRepository.save(wallet);

		historyRepository.recordTransaction(
			savedWallet.getId(),
			TransactionType.ORDER_DEDUCT,
			command.amount(),
			savedWallet.getBalance(),
			ReferenceType.PAYMENT,
			command.orderId()
		);

		eventPublisher.publish(new WalletDeductedEvent(
			savedWallet.getId(),
			command.memberId(),
			command.paymentId(),
			command.orderId(),
			command.amount(),
			LocalDateTime.now()
		));

		log.info("[Wallet] 결제 차감 완료. walletId={}, paymentId={}, amount={}, balanceAfter={}",
			savedWallet.getId(), command.paymentId(), command.amount(), savedWallet.getBalance());

		return DeductWalletResult.success(savedWallet.getId(), savedWallet.getBalance());
	}
}
