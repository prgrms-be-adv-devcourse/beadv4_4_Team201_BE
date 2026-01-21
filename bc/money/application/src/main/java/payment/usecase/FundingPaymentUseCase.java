package payment.usecase;

import app.giftify.shared.domain.vo.Money;
import payment.usecase.command.FundingContributeCommand;
import payment.usecase.result.FundingContributeResult;

/**
 * 펀딩 복합 결제 유스케이스.
 * 예치금 우선 차감 + 부족분 PG 결제 방식을 지원합니다.
 */
public interface FundingPaymentUseCase {

	/**
	 * 펀딩 참여 결제를 시작합니다.
	 * 1. 예치금에서 가능한 만큼 차감
	 * 2. 부족분이 있으면 PG 결제 정보 반환
	 *
	 * @param command 펀딩 참여 커맨드 (userId, amount)
	 * @return 복합 결제 결과 (예치금 사용액, PG 결제 필요액, 완료 여부 등)
	 */
	FundingContributeResult contribute(FundingContributeCommand command);

	/**
	 * PG 결제 실패 시 예치금을 복구합니다.
	 *
	 * @param userId 사용자 ID
	 * @param amount 복구할 예치금 금액
	 * @param paymentId 실패한 결제 ID (참조용)
	 */
	void rollbackWallet(Long userId, Money amount, Long paymentId);
}
