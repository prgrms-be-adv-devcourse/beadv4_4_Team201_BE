package app.giftify.payment.application.inbound;

/**
 * 수령 확정 유스케이스.
 *
 * <p>구현 시 권한 검증을 반드시 수행해야 합니다:</p>
 * <pre>{@code
 * if (!payment.isOwnedBy(command.requesterId())) {
 *     throw new PaymentException(PaymentErrorCode.UNAUTHORIZED_ACCESS, ...);
 * }
 * }</pre>
 */
public interface ReceivePaymentUseCase {
	void receive(ReceivePaymentCommand command);
}
