package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.inbound.PaymentDetailQuery;
import app.giftify.payment.application.inbound.PaymentDetailResult;
import app.giftify.payment.application.inbound.PaymentHistoryQuery;
import app.giftify.payment.application.inbound.PaymentSummaryResult;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.api.paging.Page;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * QueryPaymentService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class QueryPaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	private QueryPaymentService sut;

	@BeforeEach
	void setUp() {
		sut = new QueryPaymentService(paymentRepository);
	}

	private Payment createTestPayment(Long memberId) {
		return Payment.builder()
			.id(1L)
			.idempotencyKey("test-key")
			.orderId("order-123")
			.memberId(memberId)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.orderItems(List.of(new OrderItemSnapshot(
				"item-001", "테스트 상품", Money.of(10000), 1, Money.of(10000), 100L
			)))
			.status(PaymentStatus.PAID)
			.build();
	}

	@Nested
	@DisplayName("Given 결제 조회 시")
	class Given_결제_조회_시 {

		@Nested
		@DisplayName("When 본인 결제 조회하면")
		class When_본인_결제_조회하면 {

			@Test
			@DisplayName("Then 결제 상세 정보 반환")
			void Then_결제_상세_정보_반환() {
				// given
				Long memberId = 100L;
				Payment payment = createTestPayment(memberId);
				given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

				PaymentDetailQuery query = new PaymentDetailQuery(1L, memberId);

				// when
				PaymentDetailResult result = sut.getPayment(query);

				// then
				assertThat(result.paymentId()).isEqualTo(1L);
				assertThat(result.orderId()).isEqualTo("order-123");
			}
		}

		@Nested
		@DisplayName("When 타인 결제 조회하면")
		class When_타인_결제_조회하면 {

			@Test
			@DisplayName("Then UNAUTHORIZED_ACCESS 예외 발생")
			void Then_UNAUTHORIZED_ACCESS_예외_발생() {
				// given
				Payment payment = createTestPayment(100L);
				given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

				PaymentDetailQuery query = new PaymentDetailQuery(1L, 999L); // 다른 회원

				// when & then
				assertThatThrownBy(() -> sut.getPayment(query))
					.isInstanceOf(PaymentException.class)
					.hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.UNAUTHORIZED_ACCESS);
			}
		}

		@Nested
		@DisplayName("When 존재하지 않는 결제 조회하면")
		class When_존재하지_않는_결제_조회하면 {

			@Test
			@DisplayName("Then PAYMENT_NOT_FOUND 예외 발생")
			void Then_PAYMENT_NOT_FOUND_예외_발생() {
				// given
				given(paymentRepository.findById(999L)).willReturn(Optional.empty());

				PaymentDetailQuery query = new PaymentDetailQuery(999L, 100L);

				// when & then
				assertThatThrownBy(() -> sut.getPayment(query))
					.isInstanceOf(PaymentException.class)
					.hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.PAYMENT_NOT_FOUND);
			}
		}
	}

	@Nested
	@DisplayName("Given 결제 이력 조회 시")
	class Given_결제_이력_조회_시 {

		@Nested
		@DisplayName("When 페이징 조회하면")
		class When_페이징_조회하면 {

			@Test
			@DisplayName("Then 페이징된 결과 반환")
			void Then_페이징된_결과_반환() {
				// given
				Long memberId = 100L;
				Payment payment = createTestPayment(memberId);
				Page<Payment> page = Page.of(List.of(payment), 1L);

				PaymentHistoryQuery query = PaymentHistoryQuery.of(memberId, 0, 10);
				given(paymentRepository.findByMemberId(memberId, query.pageRequest()))
					.willReturn(page);

				// when
				PageResponse<PaymentSummaryResult> result = sut.getPaymentHistory(query);

				// then
				assertThat(result.content()).hasSize(1);
				assertThat(result.totalElements()).isEqualTo(1);
				assertThat(result.pageNumber()).isEqualTo(0);
				assertThat(result.pageSize()).isEqualTo(10);
			}
		}
	}
}
