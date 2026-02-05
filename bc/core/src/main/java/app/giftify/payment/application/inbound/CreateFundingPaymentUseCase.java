package app.giftify.payment.application.inbound;

/**
 * 펀딩 결제 생성 유스케이스.
 *
 * <p>펀딩 참여를 위한 결제를 생성합니다.</p>
 */
public interface CreateFundingPaymentUseCase {
	PaymentCreatedResult create(CreateFundingPaymentCommand command);
}
