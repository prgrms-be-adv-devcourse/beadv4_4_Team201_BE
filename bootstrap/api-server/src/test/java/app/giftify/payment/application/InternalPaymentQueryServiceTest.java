package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalPaymentQueryService 테스트")
class InternalPaymentQueryServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentFieldEncryptor encryptor;

	@InjectMocks
	private InternalPaymentQueryService internalPaymentQueryService;

	private Payment createTestPayment(Long id, String paymentKey, String approveCode) {
		return Payment.builder()
			.id(id)
			.orderId(1L)
			.orderNumber("ORD-001")
			.memberId(100L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.status(PaymentStatus.PAID)
			.paymentKey(paymentKey)
			.approveCode(approveCode)
			.build();
	}

	@Nested
	@DisplayName("findById 메서드")
	class FindByIdTests {

		@Test
		@DisplayName("결제 조회 성공 시 복호화된 결과를 반환한다")
		void findById_ReturnsDecryptedResult_WhenPaymentExists() {
			// given
			Long paymentId = 1L;
			Payment payment = createTestPayment(paymentId, "enc-key", "enc-code");
			given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("enc-key")).willReturn("dec-key");
			given(encryptor.decrypt("enc-code")).willReturn("dec-code");

			// when
			Optional<InternalPaymentResult> result = internalPaymentQueryService.findById(paymentId);

			// then
			assertThat(result).isPresent();
			InternalPaymentResult paymentResult = result.get();
			assertThat(paymentResult.paymentId()).isEqualTo(paymentId);
			assertThat(paymentResult.orderNumber()).isEqualTo("ORD-001");
			assertThat(paymentResult.memberId()).isEqualTo(100L);
			assertThat(paymentResult.status()).isEqualTo(PaymentStatus.PAID);
			assertThat(paymentResult.paymentKey()).isEqualTo("dec-key");
			assertThat(paymentResult.approveCode()).isEqualTo("dec-code");
		}

		@Test
		@DisplayName("존재하지 않는 paymentId는 빈 Optional을 반환한다")
		void findById_ReturnsEmpty_WhenPaymentNotFound() {
			// given
			given(paymentRepository.findById(999L)).willReturn(Optional.empty());

			// when
			Optional<InternalPaymentResult> result = internalPaymentQueryService.findById(999L);

			// then
			assertThat(result).isEmpty();
			then(encryptor).shouldHaveNoInteractions();
		}
	}

	@Nested
	@DisplayName("findByOrderNumber 메서드")
	class FindByOrderNumberTests {

		@Test
		@DisplayName("주문번호로 조회 성공 시 복호화된 결과를 반환한다")
		void findByOrderNumber_ReturnsDecryptedResult_WhenPaymentExists() {
			// given
			String orderNumber = "ORD-001";
			Payment payment = createTestPayment(1L, "enc-key", "enc-code");
			given(paymentRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("enc-key")).willReturn("dec-key");
			given(encryptor.decrypt("enc-code")).willReturn("dec-code");

			// when
			Optional<InternalPaymentResult> result = internalPaymentQueryService.findByOrderNumber(orderNumber);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().paymentKey()).isEqualTo("dec-key");
			assertThat(result.get().approveCode()).isEqualTo("dec-code");
		}

		@Test
		@DisplayName("존재하지 않는 주문번호는 빈 Optional을 반환한다")
		void findByOrderNumber_ReturnsEmpty_WhenNotFound() {
			// given
			given(paymentRepository.findByOrderNumber("NOT-EXIST")).willReturn(Optional.empty());

			// when
			Optional<InternalPaymentResult> result = internalPaymentQueryService.findByOrderNumber("NOT-EXIST");

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("decryptIfPresent 로직")
	class DecryptIfPresentTests {

		@Test
		@DisplayName("paymentKey가 null이면 복호화를 건너뛴다")
		void decryptIfPresent_SkipsDecryption_WhenPaymentKeyIsNull() {
			// given
			Payment payment = createTestPayment(1L, null, null);
			given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

			// when
			Optional<InternalPaymentResult> result = internalPaymentQueryService.findById(1L);

			// then
			assertThat(result).isPresent();
			assertThat(result.get().paymentKey()).isNull();
			assertThat(result.get().approveCode()).isNull();
			then(encryptor).shouldHaveNoInteractions();
		}

		@Test
		@DisplayName("복호화 실패 시 PaymentException을 던진다")
		void decryptIfPresent_ThrowsPaymentException_WhenDecryptionFails() {
			// given
			Payment payment = createTestPayment(1L, "enc-key", "enc-code");
			given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
			given(encryptor.decrypt("enc-key")).willThrow(new RuntimeException("decryption error"));

			// when & then
			assertThatThrownBy(() -> internalPaymentQueryService.findById(1L))
				.isInstanceOf(PaymentException.class)
				.satisfies(ex -> {
					PaymentException pe = (PaymentException) ex;
					assertThat(pe.getErrorCode()).isEqualTo(PaymentErrorCode.DECRYPTION_FAILED);
				});
		}
	}
}
