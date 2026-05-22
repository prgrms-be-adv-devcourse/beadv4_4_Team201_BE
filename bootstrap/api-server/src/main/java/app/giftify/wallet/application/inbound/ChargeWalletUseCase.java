package app.giftify.wallet.application.inbound;

/**
 * Wallet 충전 UseCase.
 * PG 결제를 통한 Wallet 잔액 충전을 처리합니다.
 */
public interface ChargeWalletUseCase {
	/**
	 * Wallet을 충전합니다.
	 *
	 * @param command 충전 요청 정보
	 * @return 충전 결과
	 */
	ChargeWalletResult charge(ChargeWalletCommand command);
}
