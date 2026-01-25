package app.giftify.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * Payment 도메인 모델 단위 테스트.
 * 실패 케이스를 우선으로 검증합니다.
 */
class PaymentTest {

	// ========== 테스트 픽스처 ========== //

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

	private Payment.Builder baseBuilder() {
		return Payment.builder()
			.idempotencyKey("test-key-123")
			.orderId("order-456")
			.memberId(1L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of(createOrderItem()))
			.status(PaymentStatus.PENDING);
	}

	private Payment createPendingPayment() {
		return baseBuilder().build();
	}

	private Payment createPaidPayment() {
		return baseBuilder().status(PaymentStatus.PAID).build();
	}

	// ========== 상태 변경 실패 케이스 ========== //

	@Nested
	@DisplayName("Given PAID 상태의 결제")
	class Given_PAID_상태의_결제 {

		@Nested
		@DisplayName("When markAsPaid 호출하면")
		class When_markAsPaid_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				Payment payment = createPaidPayment();

				assertThatThrownBy(() ->
					payment.markAsPaid("key", "code", LocalDateTime.now()))
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("결제 완료 불가능한 상태");
			}
		}

		@Nested
		@DisplayName("When markAsCanceled 호출하면")
		class When_markAsCanceled_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				Payment payment = createPaidPayment();

				assertThatThrownBy(payment::markAsCanceled)
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("취소 불가능한 상태");
			}
		}

		@Nested
		@DisplayName("When markAsFailed 호출하면")
		class When_markAsFailed_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				Payment payment = createPaidPayment();

				assertThatThrownBy(payment::markAsFailed)
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("대기 중인 결제만");
			}
		}
	}

	@Nested
	@DisplayName("Given PENDING 상태의 결제")
	class Given_PENDING_상태의_결제 {

		@Nested
		@DisplayName("When markAsRefunded 호출하면")
		class When_markAsRefunded_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				Payment payment = createPendingPayment();

				assertThatThrownBy(payment::markAsRefunded)
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("환불 불가능한 상태");
			}
		}

		@Nested
		@DisplayName("When markAsReceived 호출하면")
		class When_markAsReceived_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생")
			void Then_PaymentException_발생() {
				Payment payment = createPendingPayment();

				assertThatThrownBy(payment::markAsReceived)
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("수령 확정 불가능한 상태");
			}
		}
	}

	@Nested
	@DisplayName("Given RECEIVED 상태의 결제")
	class Given_RECEIVED_상태의_결제 {

		@Nested
		@DisplayName("When markAsRefunded 호출하면")
		class When_markAsRefunded_호출하면 {

			@Test
			@DisplayName("Then PaymentException 발생 (수령 확정 후 환불 불가)")
			void Then_PaymentException_발생() {
				Payment payment = baseBuilder().status(PaymentStatus.RECEIVED).build();

				assertThatThrownBy(payment::markAsRefunded)
					.isInstanceOf(PaymentException.class)
					.hasMessageContaining("환불 불가능한 상태");
			}
		}
	}

	// ========== 상태 변경 성공 케이스 ========== //

	@Nested
	@DisplayName("Given PENDING 상태의 결제에서 성공 케이스")
	class Given_PENDING_상태의_결제에서_성공_케이스 {

		@Test
		@DisplayName("markAsPaid 호출 시 PAID 상태로 변경")
		void markAsPaid_성공() {
			Payment payment = createPendingPayment();
			LocalDateTime paidAt = LocalDateTime.of(2026, 1, 25, 12, 0);

			payment.markAsPaid("pg-key-123", "approve-456", paidAt);

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
			assertThat(payment.getPaymentKey()).isEqualTo("pg-key-123");
			assertThat(payment.getApproveCode()).isEqualTo("approve-456");
			assertThat(payment.getPaidAt()).isEqualTo(paidAt);
		}

		@Test
		@DisplayName("markAsCanceled 호출 시 CANCELED 상태로 변경")
		void markAsCanceled_성공() {
			Payment payment = createPendingPayment();

			payment.markAsCanceled();

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
		}

		@Test
		@DisplayName("markAsFailed 호출 시 FAILED 상태로 변경")
		void markAsFailed_성공() {
			Payment payment = createPendingPayment();

			payment.markAsFailed();

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		}
	}

	@Nested
	@DisplayName("Given PAID 상태의 결제에서 성공 케이스")
	class Given_PAID_상태의_결제에서_성공_케이스 {

		@Test
		@DisplayName("markAsRefunded 호출 시 REFUNDED 상태로 변경")
		void markAsRefunded_성공() {
			Payment payment = createPaidPayment();

			payment.markAsRefunded();

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
		}

		@Test
		@DisplayName("markAsReceived 호출 시 RECEIVED 상태로 변경")
		void markAsReceived_성공() {
			Payment payment = createPaidPayment();

			payment.markAsReceived();

			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
		}
	}

	// ========== Builder 검증 실패 케이스 ========== //

	@Nested
	@DisplayName("Given Builder에서 필수 필드 누락 시")
	class Given_Builder에서_필수_필드_누락_시 {

		@Test
		@DisplayName("idempotencyKey 누락 시 PaymentException 발생")
		void idempotencyKey_누락() {
			assertThatThrownBy(() ->
				baseBuilder().idempotencyKey(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("idempotencyKey는 필수");
		}

		@Test
		@DisplayName("orderId 누락 시 PaymentException 발생")
		void orderId_누락() {
			assertThatThrownBy(() ->
				baseBuilder().orderId(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("orderId는 필수");
		}

		@Test
		@DisplayName("memberId 누락 시 PaymentException 발생")
		void memberId_누락() {
			assertThatThrownBy(() ->
				baseBuilder().memberId(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("memberId는 필수");
		}

		@Test
		@DisplayName("originAmount 누락 시 PaymentException 발생")
		void originAmount_누락() {
			assertThatThrownBy(() ->
				baseBuilder().originAmount(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("originAmount는 필수");
		}

		@Test
		@DisplayName("paidAmount 누락 시 PaymentException 발생")
		void paidAmount_누락() {
			assertThatThrownBy(() ->
				baseBuilder().paidAmount(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("paidAmount는 필수");
		}

		@Test
		@DisplayName("status 누락 시 PaymentException 발생")
		void status_누락() {
			assertThatThrownBy(() ->
				baseBuilder().status(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("status는 필수");
		}

		@Test
		@DisplayName("orderItems null 시 PaymentException 발생")
		void orderItems_null() {
			assertThatThrownBy(() ->
				baseBuilder().orderItems(null).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("orderItems는 필수");
		}

		@Test
		@DisplayName("orderItems 빈 리스트 시 PaymentException 발생")
		void orderItems_빈_리스트() {
			assertThatThrownBy(() ->
				baseBuilder().orderItems(Collections.emptyList()).build())
				.isInstanceOf(PaymentException.class)
				.hasMessageContaining("orderItems는 필수");
		}
	}

	// ========== 상태 조회 메서드 ========== //

	@Nested
	@DisplayName("Given 상태 조회 메서드")
	class Given_상태_조회_메서드 {

		@Test
		@DisplayName("PAID 상태에서 isRefundable은 true")
		void PAID_상태에서_isRefundable_true() {
			Payment payment = createPaidPayment();
			assertThat(payment.isRefundable()).isTrue();
		}

		@Test
		@DisplayName("PENDING 상태에서 isRefundable은 false")
		void PENDING_상태에서_isRefundable_false() {
			Payment payment = createPendingPayment();
			assertThat(payment.isRefundable()).isFalse();
		}

		@Test
		@DisplayName("PENDING 상태에서 isCancelable은 true")
		void PENDING_상태에서_isCancelable_true() {
			Payment payment = createPendingPayment();
			assertThat(payment.isCancelable()).isTrue();
		}

		@Test
		@DisplayName("PAID 상태에서 isCancelable은 false")
		void PAID_상태에서_isCancelable_false() {
			Payment payment = createPaidPayment();
			assertThat(payment.isCancelable()).isFalse();
		}
	}

	// ========== equals / hashCode ========== //

	@Nested
	@DisplayName("Given equals/hashCode")
	class Given_equals_hashCode {

		@Test
		@DisplayName("동일한 id를 가진 Payment는 동등")
		void 동일_id_동등() {
			Payment payment1 = baseBuilder().id(1L).idempotencyKey("key-1").build();
			Payment payment2 = baseBuilder().id(1L).idempotencyKey("key-2").build();

			assertThat(payment1).isEqualTo(payment2);
			assertThat(payment1.hashCode()).isEqualTo(payment2.hashCode());
		}

		@Test
		@DisplayName("id가 null이면 idempotencyKey로 비교")
		void id_null이면_idempotencyKey_비교() {
			Payment payment1 = baseBuilder().idempotencyKey("same-key").build();
			Payment payment2 = baseBuilder().idempotencyKey("same-key").build();

			assertThat(payment1).isEqualTo(payment2);
		}

		@Test
		@DisplayName("다른 idempotencyKey는 다른 Payment")
		void 다른_idempotencyKey_다름() {
			Payment payment1 = baseBuilder().idempotencyKey("key-1").build();
			Payment payment2 = baseBuilder().idempotencyKey("key-2").build();

			assertThat(payment1).isNotEqualTo(payment2);
		}
	}
}