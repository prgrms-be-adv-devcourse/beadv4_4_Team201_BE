package app.giftify.payment.adapter.out.pg;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.giftify.payment.adapter.config.TossPaymentsProperties;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;

@Component
public class TossPaymentsClient {

	private static final Logger log = LoggerFactory.getLogger(TossPaymentsClient.class);
	private static final String CONFIRM_URL = "/v1/payments/confirm";

	private final RestTemplate restTemplate;
	private final TossPaymentsProperties properties;
	private final ObjectMapper objectMapper;

	public TossPaymentsClient(
		RestTemplate tossPaymentsRestTemplate,
		TossPaymentsProperties properties,
		ObjectMapper objectMapper
	) {
		this.restTemplate = tossPaymentsRestTemplate;
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	/**
	 * Toss Payments 결제 승인 API를 호출합니다.
	 *
	 * @param paymentKey Toss SDK에서 받은 결제 키
	 * @param orderId    서버에서 생성한 주문 ID
	 * @param amount     결제 금액
	 * @return 승인 성공 시 true
	 * @throws PaymentException 승인 실패 시
	 */
	public TossConfirmResult confirm(String paymentKey, String orderId, BigDecimal amount) {
		String url = properties.getApi().getBaseUrl() + CONFIRM_URL;

		HttpHeaders headers = createHeaders();
		TossConfirmRequest requestBody = new TossConfirmRequest(paymentKey, orderId, amount.longValue());

		try {
			HttpEntity<TossConfirmRequest> entity = new HttpEntity<>(requestBody, headers);
			String response = restTemplate.postForObject(url, entity, String.class);

			log.info("[TossPayments] 결제 승인 성공. paymentKey={}, orderId={}", paymentKey, orderId);
			return TossConfirmResult.success(paymentKey);

		} catch (HttpClientErrorException e) {
			return handleTossError(e, paymentKey, orderId);
		} catch (Exception e) {
			log.error("[TossPayments] 결제 승인 중 예외 발생. paymentKey={}, orderId={}", paymentKey, orderId, e);
			throw new PaymentException(PaymentErrorCode.PG_CONNECTION_ERROR,
				"PG사 연결 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Basic Auth: secretKey + ":" 를 Base64 인코딩
		String credentials = properties.getSecretKey() + ":";
		String encodedCredentials = Base64.getEncoder()
			.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		headers.set("Authorization", "Basic " + encodedCredentials);

		return headers;
	}

	private TossConfirmResult handleTossError(HttpClientErrorException e, String paymentKey, String orderId) {
		String errorCode = "UNKNOWN";
		String errorMessage = "알 수 없는 오류";

		try {
			JsonNode errorBody = objectMapper.readTree(e.getResponseBodyAsString());
			errorCode = errorBody.path("code").asText("UNKNOWN");
			errorMessage = errorBody.path("message").asText("알 수 없는 오류");
		} catch (Exception parseEx) {
			log.warn("[TossPayments] 에러 응답 파싱 실패", parseEx);
		}

		log.warn("[TossPayments] 결제 승인 실패. paymentKey={}, orderId={}, errorCode={}, message={}",
			paymentKey, orderId, errorCode, errorMessage);

		if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
			// 클라이언트 오류 (금액 불일치, orderId 불일치 등)
			return TossConfirmResult.failure(errorCode, errorMessage);
		}

		// 서버 오류 또는 기타 오류
		throw new PaymentException(PaymentErrorCode.PG_APPROVAL_FAILED,
			String.format("PG사 승인 실패 [%s]: %s", errorCode, errorMessage));
	}

	private record TossConfirmRequest(String paymentKey, String orderId, Long amount) {
	}
}