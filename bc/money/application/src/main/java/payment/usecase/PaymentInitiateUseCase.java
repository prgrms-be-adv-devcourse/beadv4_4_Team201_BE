package payment.usecase;

import app.giftify.shared.domain.vo.Money;
import payment.usecase.command.PaymentInitiateCommand;
import payment.usecase.result.PaymentInitiateResult;

/**
 * 결제 시작 유스케이스.
 * Order BC로부터 결제 요청을 받아 처리합니다.
 * 현재는 예치금 전용 결제만 지원하며, 복합 결제(예치금 + PG)는 향후 활성화 예정입니다.
 */
public interface PaymentInitiateUseCase {

	/**
	 * 결제를 시작합니다.
	 * 1. 예치금에서 가능한 만큼 차감
	 * 2. 부족분이 있으면 예외 발생 (현재: 예치금 전용 결제)
	 *
	 * @param command 결제 시작 커맨드 (userId, orderId, amount, paymentType)
	 * @return 결제 시작 결과 (예치금 사용액, PG 결제 필요액, 완료 여부 등)
	 */
	PaymentInitiateResult initiate(PaymentInitiateCommand command);

	/**
	 * PG 결제 실패 시 예치금을 복구합니다.
	 *
	 * @param userId 사용자 ID
	 * @param amount 복구할 예치금 금액
	 * @param paymentId 실패한 결제 ID (참조용)
	 */
	void rollbackWallet(Long userId, Money amount, Long paymentId);
}