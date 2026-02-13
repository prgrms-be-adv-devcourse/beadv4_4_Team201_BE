package app.giftify.payment.application.inbound;

import java.util.Optional;

public interface InternalPaymentQueryUseCase {

	Optional<InternalPaymentResult> findById(Long paymentId);

	Optional<InternalPaymentResult> findByOrderId(String orderId);

}
