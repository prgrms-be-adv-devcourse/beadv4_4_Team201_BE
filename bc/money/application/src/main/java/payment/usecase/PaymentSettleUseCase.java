package payment.usecase;

/**
 * 결제 정산(확정) 유스케이스.
 * 펀딩이 성공적으로 완료되어 결제가 확정될 때 사용합니다.
 */
public interface PaymentSettleUseCase {
	void settle(Long paymentId);
}
