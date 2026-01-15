package payment.usecase;

public interface PaymentCompleteUseCase {
	void complete(Long paymentId, String pgTransactionId, boolean isSuccess);
}
