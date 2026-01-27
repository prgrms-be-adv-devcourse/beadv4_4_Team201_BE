package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * InternalPaymentQueryService 단위 테스트.
 *
 * <p>복호화 로직과 조회 기능을 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class InternalPaymentQueryServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentFieldEncryptor encryptor;

	private InternalPaymentQueryService sut;

	@BeforeEach
	void setUp() {
		sut = new InternalPaymentQueryService(paymentRepository, encryptor);
	}

	// ========== 테스트 픽스처 ========== //

	private Payment createPaidPayment() {
		return Payment.builder()
			.id(1L)
			.idempotencyKey("test-key-123")
			.orderId("order-456")
			.memberId(100L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of(createOrderItem()))
			.status(PaymentStatus.PAID)
			.paymentKey("encrypted-payment-key")
			.approveCode("encrypted-approve-code")
			.build();
	}

	private Payment createPendingPayment() {
		OrderItemSnapshot item = new OrderItemSnapshot(
			"item-002",
			"대기 상품",
			Money.of(5000),
			1,
			Money.of(5000),
			200L
		);
		return Payment.builder()
			.id(2L)
			.idempotencyKey("pending-key-456")
			.orderId("order-789")
			.memberId(200L)
			.type(PaymentType.POINT_CHARGE)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(5000))
			.paidAmount(Money.of(5000))
			.orderItems(List.of(item))
			.status(PaymentStatus.PENDING)
			.build();
	}

	private OrderItemSnapshot createOrderItem() {
		return new OrderItemSnapshot(
			"item-001",
			"테스트 상품",
			Money.of(10000),
			1,
			Money.of(10000),
			100L
		);
	}

	// ========== findById 테스트 ========== //

	@Nested
	@DisplayName("Given 암호화된 paymentKey와 approveCode가 있는 결제")
	class Given_암호화된_결제정보가_있는_결제 {

		@Nested
		@DisplayName("When findById 호출하면")
		class When_findById_호출하면 {

			@Test
			@DisplayName("Then 복호화된 값이 반환된다")
			void Then_복호화된_값이_반환된다() {
				// given
				Payment payment = createPaidPayment();
				given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
				given(encryptor.decrypt("encrypted-payment-key")).willReturn("decrypted-payment-key");
				given(encryptor.decrypt("encrypted-approve-code")).willReturn("decrypted-approve-code");

				// when
				Optional<InternalPaymentResult> result = sut.findById(1L);

				// then
				assertThat(result).isPresent();
				assertThat(result.get().paymentKey()).isEqualTo("decrypted-payment-key");
				assertThat(result.get().approveCode()).isEqualTo("decrypted-approve-code");
				verify(encryptor).decrypt("encrypted-payment-key");
				verify(encryptor).decrypt("encrypted-approve-code");
			}
		}
	}

	@Nested
	@DisplayName("Given paymentKey가 null인 PENDING 상태 결제")
	class Given_paymentKey가_null인_결제 {

		@Nested
		@DisplayName("When findById 호출하면")
		class When_findById_호출하면 {

			@Test
			@DisplayName("Then null 그대로 반환되고 복호화는 호출되지 않는다")
			void Then_null_그대로_반환된다() {
				// given
				Payment payment = createPendingPayment();
				given(paymentRepository.findById(2L)).willReturn(Optional.of(payment));

				// when
				Optional<InternalPaymentResult> result = sut.findById(2L);

				// then
				assertThat(result).isPresent();
				assertThat(result.get().paymentKey()).isNull();
				assertThat(result.get().approveCode()).isNull();
				assertThat(result.get().status()).isEqualTo(PaymentStatus.PENDING);
			}
		}
	}

	@Nested
	@DisplayName("Given 존재하지 않는 결제 ID")
	class Given_존재하지_않는_결제_ID {

		@Nested
		@DisplayName("When findById 호출하면")
		class When_findById_호출하면 {

			@Test
			@DisplayName("Then empty Optional 반환")
			void Then_empty_Optional_반환() {
				// given
				given(paymentRepository.findById(999L)).willReturn(Optional.empty());

				// when
				Optional<InternalPaymentResult> result = sut.findById(999L);

				// then
				assertThat(result).isEmpty();
			}
		}
	}

	// ========== findByOrderId 테스트 ========== //

	@Nested
	@DisplayName("Given 특정 주문에 여러 결제가 있을 때")
	class Given_주문에_여러_결제가_있을_때 {

		@Nested
		@DisplayName("When findByOrderId 호출하면")
		class When_findByOrderId_호출하면 {

			@Test
			@DisplayName("Then 모든 결제가 복호화되어 반환된다")
			void Then_모든_결제가_복호화되어_반환된다() {
				// given
				Payment payment = createPaidPayment();
				given(paymentRepository.findByOrderId("order-456")).willReturn(List.of(payment));
				given(encryptor.decrypt("encrypted-payment-key")).willReturn("decrypted-payment-key");
				given(encryptor.decrypt("encrypted-approve-code")).willReturn("decrypted-approve-code");

				// when
				List<InternalPaymentResult> results = sut.findByOrderId("order-456");

				// then
				assertThat(results).hasSize(1);
				assertThat(results.get(0).paymentKey()).isEqualTo("decrypted-payment-key");
			}
		}
	}

	// ========== findByIdempotencyKey 테스트 ========== //

	@Nested
	@DisplayName("Given 멱등성 키로 조회 시")
	class Given_멱등성_키로_조회_시 {

		@Nested
		@DisplayName("When findByIdempotencyKey 호출하면")
		class When_findByIdempotencyKey_호출하면 {

			@Test
			@DisplayName("Then 복호화된 결제 정보 반환")
			void Then_복호화된_결제_정보_반환() {
				// given
				Payment payment = createPaidPayment();
				given(paymentRepository.findByIdempotencyKey("test-key-123")).willReturn(Optional.of(payment));
				given(encryptor.decrypt("encrypted-payment-key")).willReturn("decrypted-payment-key");
				given(encryptor.decrypt("encrypted-approve-code")).willReturn("decrypted-approve-code");

				// when
				Optional<InternalPaymentResult> result = sut.findByIdempotencyKey("test-key-123");

				// then
				assertThat(result).isPresent();
				assertThat(result.get().idempotencyKey()).isEqualTo("test-key-123");
				assertThat(result.get().paymentKey()).isEqualTo("decrypted-payment-key");
			}
		}
	}

	// ========== 빈 문자열 복호화 테스트 ========== //

	@Nested
	@DisplayName("Given paymentKey가 빈 문자열인 결제")
	class Given_paymentKey가_빈_문자열인_결제 {

		@Nested
		@DisplayName("When findById 호출하면")
		class When_findById_호출하면 {

			@Test
			@DisplayName("Then null 반환되고 복호화는 호출되지 않는다")
			void Then_null_반환되고_복호화는_호출되지_않는다() {
				// given
				Payment payment = Payment.builder()
					.id(3L)
					.idempotencyKey("empty-key-789")
					.orderId("order-empty")
					.memberId(300L)
					.type(PaymentType.FUNDING)
					.method(PaymentMethod.CARD)
					.originAmount(Money.of(10000))
					.paidAmount(Money.of(10000))
					.orderItems(List.of(createOrderItem()))
					.status(PaymentStatus.PENDING)
					.paymentKey("")  // 빈 문자열
					.approveCode("") // 빈 문자열
					.build();
				given(paymentRepository.findById(3L)).willReturn(Optional.of(payment));

				// when
				Optional<InternalPaymentResult> result = sut.findById(3L);

				// then
				assertThat(result).isPresent();
				assertThat(result.get().paymentKey()).isNull();
				assertThat(result.get().approveCode()).isNull();
				verifyNoInteractions(encryptor);
			}
		}
	}

	// ========== 복호화 실패 테스트 ========== //

	@Nested
	@DisplayName("Given 복호화 실패 시")
	class Given_복호화_실패_시 {

		@Nested
		@DisplayName("When findById 호출하면")
		class When_findById_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				// given
				Payment payment = createPaidPayment();
				given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));
				given(encryptor.decrypt("encrypted-payment-key"))
					.willThrow(new RuntimeException("Decryption failed"));

				// when & then
				assertThatThrownBy(() -> sut.findById(1L))
					.isInstanceOf(PaymentException.class)
					.hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.DECRYPTION_FAILED);
			}
		}
	}
}
