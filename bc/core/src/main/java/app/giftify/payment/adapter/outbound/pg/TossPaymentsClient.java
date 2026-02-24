package app.giftify.payment.adapter.outbound.pg;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

/**
 * Toss Payments API 클라이언트.
 *
 * <p>HTTP Interface를 통해 Toss Payments API를 호출하고,
 * 에러 핸들링 및 결과 변환을 담당합니다.</p>
 */
@Component
public class TossPaymentsClient {

	private static final Logger log = LoggerFactory.getLogger(TossPaymentsClient.class);

	private final TossPaymentsApi tossPaymentsApi;

	public TossPaymentsClient(TossPaymentsApi tossPaymentsApi) {
		this.tossPaymentsApi = tossPaymentsApi;
	}

	/**
	 * Toss Payments 결제 승인 API를 호출합니다.
	 *
	 * @param paymentKey Toss SDK에서 받은 결제 키
	 * @param orderId    서버에서 생성한 주문 ID
	 * @param amount     결제 금액
	 * @return 승인 결과
	 * @throws PaymentException PG사 연결 오류 또는 승인 실패 시
	 */
	public TossConfirmResult confirm(String paymentKey, String orderId, BigDecimal amount) {
		try {
			TossPaymentsApi.TossConfirmRequest request = new TossPaymentsApi.TossConfirmRequest(
				paymentKey,
				orderId,
				amount.longValue()
			);

			TossPaymentsApi.TossPaymentResponse response = tossPaymentsApi.confirm(request);

			if (response.isSuccess()) {
				log.info("[TossPayments] 결제 승인 성공. paymentKey={}, orderId={}", paymentKey, orderId);
				String approveNo = response.card() != null ? response.card().approveNo() : null;
				return TossConfirmResult.success(paymentKey, response.lastTransactionKey(), approveNo);
			} else {
				log.warn("[TossPayments] 결제 승인 실패. paymentKey={}, orderId={}, errorCode={}, message={}",
					paymentKey, orderId, response.code(), response.message());
				return TossConfirmResult.failure(response.code(), response.message());
			}

		} catch (HttpClientErrorException e) {
			return handleConfirmError(e, paymentKey, orderId);
		} catch (Exception e) {
			log.error("[TossPayments] 결제 승인 중 예외 발생. paymentKey={}, orderId={}", paymentKey, orderId, e);
			throw new PaymentException(PaymentErrorCode.PG_CONNECTION_ERROR,
				"PG사 연결 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private TossConfirmResult handleConfirmError(HttpClientErrorException e, String paymentKey, String orderId) {
		log.warn("[TossPayments] 결제 승인 HTTP 오류. paymentKey={}, orderId={}, status={}, body={}",
			paymentKey, orderId, e.getStatusCode(), e.getResponseBodyAsString());

		if (e.getStatusCode().is4xxClientError()) {
			return TossConfirmResult.failure("CLIENT_ERROR", e.getResponseBodyAsString());
		}

		throw new PaymentException(PaymentErrorCode.PG_APPROVAL_FAILED,
			"PG사 승인 실패: " + e.getMessage());
	}

	/**
	 * Toss Payments 결제 취소 API를 호출합니다.
	 *
	 * @param paymentKey   취소할 결제의 paymentKey
	 * @param cancelReason 취소 사유
	 * @return 취소 결과
	 * @throws PaymentException 서버 오류 또는 연결 실패 시
	 */
	public TossCancelResult cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
		try {
			TossPaymentsApi.TossCancelRequest request = new TossPaymentsApi.TossCancelRequest(cancelReason, cancelAmount);

			TossPaymentsApi.TossPaymentResponse response = tossPaymentsApi.cancel(paymentKey, request);

			if (response.isSuccess()) {
				log.info("[TossPayments] 결제 취소 성공. paymentKey={}", paymentKey);
				var cancels = response.cancels() != null
					? response.cancels().stream()
						.map(c -> new TossCancelResult.CancelDetail(
							c.transactionKey(), c.cancelAmount(), c.canceledAt()))
						.toList()
					: java.util.List.<TossCancelResult.CancelDetail>of();
				return TossCancelResult.success(paymentKey, response.lastTransactionKey(), cancels);
			} else {
				log.warn("[TossPayments] 결제 취소 실패. paymentKey={}, errorCode={}, message={}",
					paymentKey, response.code(), response.message());
				return TossCancelResult.failure(response.code(), response.message());
			}

		} catch (HttpClientErrorException e) {
			return handleCancelError(e, paymentKey);
		} catch (Exception e) {
			log.error("[TossPayments] 결제 취소 중 예외 발생. paymentKey={}", paymentKey, e);
			throw new PaymentException(PaymentErrorCode.PG_CONNECTION_ERROR,
				"PG사 연결 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private TossCancelResult handleCancelError(HttpClientErrorException e, String paymentKey) {
		log.warn("[TossPayments] 결제 취소 HTTP 오류. paymentKey={}, status={}, body={}",
			paymentKey, e.getStatusCode(), e.getResponseBodyAsString());

		if (e.getStatusCode().is4xxClientError()) {
			return TossCancelResult.failure("CLIENT_ERROR", e.getResponseBodyAsString());
		}

		throw new PaymentException(PaymentErrorCode.PG_APPROVAL_FAILED,
			"PG사 취소 실패: " + e.getMessage());
	}
}
