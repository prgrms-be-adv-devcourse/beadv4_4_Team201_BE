package app.giftify.payment.adapter.outbound.pg;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Toss Payments API를 위한 HTTP Interface.
 *
 * <p>Spring 6의 HTTP Interface를 사용하여 선언적으로 외부 API를 호출합니다.
 * RestClient와 함께 사용하여 동기 방식으로 동작합니다.</p>
 */
@HttpExchange
public interface TossPaymentsApi {

	/**
	 * 결제 승인 API를 호출합니다.
	 *
	 * @param request 승인 요청 정보 (paymentKey, orderId, amount)
	 * @return 승인 결과 응답
	 */
	@PostExchange("/v1/payments/confirm")
	TossPaymentResponse confirm(@RequestBody TossConfirmRequest request);

	/**
	 * 결제 취소 API를 호출합니다.
	 *
	 * @param paymentKey 취소할 결제의 paymentKey
	 * @param request    취소 요청 정보 (cancelReason)
	 * @return 취소 결과 응답
	 */
	@PostExchange("/v1/payments/{paymentKey}/cancel")
	TossPaymentResponse cancel(
		@PathVariable String paymentKey,
		@RequestBody TossCancelRequest request
	);

	record TossConfirmRequest(
		String paymentKey,
		String orderId,
		Long amount
	) {
	}

	record TossCancelRequest(
		String cancelReason,
		@JsonInclude(JsonInclude.Include.NON_NULL) Long cancelAmount
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record TossPaymentResponse(
		String paymentKey,
		String orderId,
		String status,
		String lastTransactionKey,
		String approvedAt,
		CardInfo card,
		String code,
		String message
	) {
		public boolean isSuccess() {
			return code == null;
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		record CardInfo(String approveNo) {}
	}
}
