package app.giftify.payment.adapter.inbound.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import app.giftify.payment.adapter.inbound.web.dto.PaymentInfoResponse;
import app.giftify.payment.application.inbound.InternalPaymentQueryUseCase;
import app.giftify.security.common.annotation.InternalApiOnly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 내부 서비스 간 통신을 위한 Payment API.
 *
 * <p>Order BC, Funding BC, Settlement BC 등이 동기 통신으로
 * 결제 정보를 조회할 때 사용합니다.</p>
 *
 * <h3>보안</h3>
 * <ul>
 *   <li>{@code @InternalApiOnly} 어노테이션으로 ROLE_INTERNAL_SERVICE 역할 필요</li>
 *   <li>프로덕션에서는 네트워크 레벨 보안(VPC, Security Group)을 병행</li>
 * </ul>
 *
 * @see app.giftify.security.common.annotation.InternalApiOnly
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/payments")
@RequiredArgsConstructor
@InternalApiOnly
@Validated
public class InternalPaymentController {

	private final InternalPaymentQueryUseCase internalPaymentQueryUseCase;

	@GetMapping("/{paymentId}")
	public ResponseEntity<PaymentInfoResponse> getById(
		@PathVariable("paymentId") @NotNull Long paymentId
	) {
		log.debug("[InternalPaymentController] 결제 조회 요청. paymentId={}", paymentId);

		return internalPaymentQueryUseCase.findById(paymentId)
			.map(PaymentInfoResponse::from)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/by-order/{orderId}")
	public ResponseEntity<List<PaymentInfoResponse>> getByOrderId(
		@PathVariable("orderId") @NotBlank String orderId
	) {
		log.debug("[InternalPaymentController] 주문별 결제 조회 요청. orderId=***{}",
			orderId != null && orderId.length() > 4 ? orderId.substring(orderId.length() - 4) : "****");

		List<PaymentInfoResponse> payments = internalPaymentQueryUseCase
			.findByOrderId(orderId)
			.stream()
			.map(PaymentInfoResponse::from)
			.toList();

		return ResponseEntity.ok(payments);
	}

	@GetMapping("/by-idempotency-key/{key}")
	public ResponseEntity<PaymentInfoResponse> getByIdempotencyKey(
		@PathVariable("key") @NotBlank String key
	) {
		log.debug("[InternalPaymentController] 멱등성 키로 결제 조회 요청. idempotencyKey=***{}",
			key != null && key.length() > 4 ? key.substring(key.length() - 4) : "****");

		return internalPaymentQueryUseCase.findByIdempotencyKey(key)
			.map(PaymentInfoResponse::from)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}
}
