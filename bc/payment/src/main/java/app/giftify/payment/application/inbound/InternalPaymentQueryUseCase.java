package app.giftify.payment.application.inbound;

import java.util.List;
import java.util.Optional;

/**
 * 내부 BC 간 결제 조회를 위한 UseCase.
 *
 * <p>Order BC, Funding BC, Settlement BC 등에서 결제 정보를 동기 조회할 때 사용
 * 모든 메서드는 PG사 민감 정보(paymentKey, approveCode)를 복호화하여 반환</p>
 */
public interface InternalPaymentQueryUseCase {

	Optional<InternalPaymentResult> findById(Long paymentId);

	List<InternalPaymentResult> findByOrderId(String orderId);

	Optional<InternalPaymentResult> findByIdempotencyKey(String idempotencyKey);
}
