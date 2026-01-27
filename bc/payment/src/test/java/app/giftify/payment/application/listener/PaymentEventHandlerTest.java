package app.giftify.payment.application.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentPaidEvent;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCanceledForOrder;
import app.giftify.shared.domain.event.payment.PaymentCompletedForFunding;
import app.giftify.shared.domain.event.payment.PaymentConfirmedForOrder;
import app.giftify.shared.domain.event.payment.PaymentRefundedForSettlement;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

	@Mock
	private EventPublisher eventPublisher;

	@Mock
	private PaymentRepository paymentRepository;

	@Captor
	private ArgumentCaptor<PaymentCompletedForFunding> fundingEventCaptor;

	@Captor
	private ArgumentCaptor<PaymentConfirmedForOrder> orderEventCaptor;

	private PaymentEventHandler sut;

	@BeforeEach
	void setUp() {
		sut = new PaymentEventHandler(eventPublisher, paymentRepository);
	}

	@Nested
	@DisplayName("Given FUNDING 타입 결제 완료 이벤트")
	class Given_FUNDING_타입_결제_완료_이벤트 {

		@Nested
		@DisplayName("When 이벤트 수신하면")
		class When_이벤트_수신하면 {

			@Test
			@DisplayName("Then orderId를 그대로 전달하는 PaymentCompletedForFunding 이벤트 발행")
			void Then_PaymentCompletedForFunding_이벤트_발행() {
				// given
				String orderId = "funding-order-123";
				PaymentPaidEvent event = new PaymentPaidEvent(
					1L, 100L, orderId, PaymentType.FUNDING,
					Money.of(10000), LocalDateTime.now()
				);

				// when
				sut.handle(event);

				// then
				verify(eventPublisher).publish(fundingEventCaptor.capture());
				PaymentCompletedForFunding captured = fundingEventCaptor.getValue();
				assertThat(captured.orderId()).isEqualTo(orderId);
				assertThat(captured.participantId()).isEqualTo(100L);
				assertThat(captured.paymentId()).isEqualTo(1L);
				assertThat(captured.amount()).isEqualTo(Money.of(10000));
			}
		}
	}

	@Nested
	@DisplayName("Given POINT_CHARGE 타입 결제 완료 이벤트")
	class Given_POINT_CHARGE_타입_결제_완료_이벤트 {

		@Nested
		@DisplayName("When 이벤트 수신하면")
		class When_이벤트_수신하면 {

			@Test
			@DisplayName("Then PaymentConfirmedForOrder 이벤트 발행")
			void Then_PaymentConfirmedForOrder_이벤트_발행() {
				// given
				PaymentPaidEvent event = new PaymentPaidEvent(
					1L, 100L, "order-123", PaymentType.POINT_CHARGE,
					Money.of(10000), LocalDateTime.now()
				);

				// when
				sut.handle(event);

				// then
				verify(eventPublisher).publish(orderEventCaptor.capture());
				PaymentConfirmedForOrder captured = orderEventCaptor.getValue();
				assertThat(captured.orderId()).isEqualTo("order-123");
			}
		}
	}

	@Nested
	@DisplayName("Given 결제 취소 이벤트")
	class Given_결제_취소_이벤트 {

		@Test
		@DisplayName("Then PaymentCanceledForOrder 이벤트 발행")
		void Then_PaymentCanceledForOrder_이벤트_발행() {
			// given
			PaymentCanceledEvent event = new PaymentCanceledEvent(
				1L, 100L, "order-123", PaymentType.FUNDING,
				Money.of(10000), "사용자 요청", LocalDateTime.now()
			);

			// when
			sut.handle(event);

			// then
			verify(eventPublisher).publish(any(PaymentCanceledForOrder.class));
		}
	}

	@Nested
	@DisplayName("Given 결제 환불 이벤트")
	class Given_결제_환불_이벤트 {

		@Test
		@DisplayName("Then PaymentRefundedForSettlement 이벤트 발행")
		void Then_PaymentRefundedForSettlement_이벤트_발행() {
			// given
			Payment payment = Payment.builder()
				.id(1L)
				.idempotencyKey("test-key")
				.orderId("order-123")
				.memberId(100L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.orderItems(List.of(new OrderItemSnapshot(
					"item-001", "상품", Money.of(10000), 1, Money.of(10000), 200L
				)))
				.status(PaymentStatus.REFUNDED)
				.build();

			given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

			PaymentRefundedEvent event = new PaymentRefundedEvent(
				1L, 100L, "order-123", PaymentType.FUNDING,
				Money.of(10000), "환불 사유", LocalDateTime.now()
			);

			// when
			sut.handle(event);

			// then
			verify(eventPublisher).publish(any(PaymentRefundedForSettlement.class));
		}
	}
}
