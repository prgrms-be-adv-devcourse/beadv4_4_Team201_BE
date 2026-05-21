package app.giftify.wallet.application.inbound;

public interface CreateWalletUseCase {

	/**
	 * 회원의 지갑을 생성합니다.
	 * 이미 지갑이 존재하는 경우 아무 작업도 수행하지 않습니다.
	 *
	 * @param memberId 회원 ID
	 * @return 생성된 지갑 정보
	 */
	CreateWalletResult createIfNotExists(Long memberId);

	record CreateWalletResult(
		Long walletId,
		Long memberId,
		boolean created
	) {
		public static CreateWalletResult created(Long walletId, Long memberId) {
			return new CreateWalletResult(walletId, memberId, true);
		}

		public static CreateWalletResult alreadyExists(Long walletId, Long memberId) {
			return new CreateWalletResult(walletId, memberId, false);
		}
	}
}
