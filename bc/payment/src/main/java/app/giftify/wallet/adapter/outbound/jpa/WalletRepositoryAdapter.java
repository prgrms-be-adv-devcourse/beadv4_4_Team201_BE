package app.giftify.wallet.adapter.outbound.jpa;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.giftify.wallet.adapter.outbound.jpa.entity.JpaWallet;
import app.giftify.wallet.application.outbound.WalletRepository;
import app.giftify.wallet.domain.Wallet;
import app.giftify.wallet.domain.WalletErrorCode;
import app.giftify.wallet.domain.WalletException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {

	private final JpaWalletRepository jpaWalletRepository;

	@Override
	public Wallet save(Wallet wallet) {
		if (wallet.getId() == null) {
			JpaWallet entity = JpaWallet.from(wallet.snapshot());
			JpaWallet saved = jpaWalletRepository.save(entity);
			return Wallet.restore(saved.toSnapshot());
		}

		JpaWallet entity = jpaWalletRepository.findById(wallet.getId())
			.orElseThrow(() -> new WalletException(WalletErrorCode.WALLET_NOT_FOUND));
		entity.updateFrom(wallet.snapshot());
		return Wallet.restore(entity.toSnapshot());
	}

	@Override
	public Optional<Wallet> findById(Long id) {
		return jpaWalletRepository.findById(id)
			.map(entity -> Wallet.restore(entity.toSnapshot()));
	}

	@Override
	public Optional<Wallet> findByMemberId(Long memberId) {
		return jpaWalletRepository.findByMemberId(memberId)
			.map(entity -> Wallet.restore(entity.toSnapshot()));
	}
}
