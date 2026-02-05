package app.giftify.payment.application.inbound;

/**
 * 예치금 충전 유스케이스.
 */
public interface ChargeDepositUseCase {
	PaymentCreatedResult charge(ChargeDepositCommand command);
}
