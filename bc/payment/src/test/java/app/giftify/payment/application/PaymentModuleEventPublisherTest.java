package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.*;
import app.giftify.shared.domain.type.CancelType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentModuleEventPublisher 테스트")
class PaymentModuleEventPublisherTest {

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private PaymentModuleEventPublisher publisher;

	private Payment createPendingPayment(Money walletDeducted) {
		return Payment.builder()
			.id(1L)
			.orderId(100L)
			.orderNumber("ORD-001")
			.memberId(200L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.walletDeductedAmount(walletDeducted)
			.status(PaymentStatus.PENDING)
			.build();
	}

	private Payment createPaidPayment(Money walletDeducted) {
		return Payment.builder()
			.id(1L)
			.orderId(100L)
			.orderNumber("ORD-001")
			.memberId(200L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.CARD)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.walletDeductedAmount(walletDeducted)
			.status(PaymentStatus.PAID)
			.paymentKey("enc-key")
			.lastTransactionKey("txn-001")
			.approveCode("approve-001")
			.paidAt(LocalDateTime.now())
			.build();
	}

	@Nested
	@DisplayName("Completed 도메인 이벤트 변환")
	class CompletedTests {

		@Test
		@DisplayName("PaymentDomainEvent.Completed → PaymentSucceededEvent로 변환하여 발행한다")
		void publishFrom_Completed_PublishesPaymentSucceededEvent() {
			// given
			Payment original = createPendingPayment(Money.zero());
			Payment completed = original.complete("enc-key", "approve-001", "txn-001", LocalDateTime.now());

			// when
			publisher.publishFrom(completed, original);

			// then
			ArgumentCaptor<PaymentSucceededEvent> captor = ArgumentCaptor.forClass(PaymentSucceededEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentSuccessData data = captor.getValue().data();
			assertThat(data.paymentId()).isEqualTo(1L);
			assertThat(data.orderId()).isEqualTo(100L);
			assertThat(data.memberId()).isEqualTo(200L);
			assertThat(data.orderNumber()).isEqualTo("ORD-001");
			assertThat(data.paidAmount()).isEqualTo(Money.of(10000));
			assertThat(data.paymentMethod()).isEqualTo(PaymentMethod.CARD);
			assertThat(data.paymentType()).isEqualTo(PaymentType.FUNDING);
			assertThat(data.paymentKey()).isEqualTo("enc-key");
			assertThat(data.transactionKey()).isEqualTo("txn-001");
		}
	}

	@Nested
	@DisplayName("Failed 도메인 이벤트 변환")
	class FailedTests {

		@Test
		@DisplayName("PaymentDomainEvent.Failed → PaymentFailedEvent로 변환하고 walletDeductedAmount를 enrichment한다")
		void publishFrom_Failed_EnrichesWalletDeductedAmount() {
			// given
			Money walletDeducted = Money.of(3000);
			Payment original = createPendingPayment(walletDeducted);
			Payment failed = original.fail();

			// when
			publisher.publishFrom(failed, original);

			// then
			ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentFailureData data = captor.getValue().data();
			assertThat(data.paymentId()).isEqualTo(1L);
			assertThat(data.walletDeductedAmount()).isEqualTo(walletDeducted);
			assertThat(data.paymentMethod()).isEqualTo(PaymentMethod.CARD);
		}

		@Test
		@DisplayName("walletDeductedAmount가 0이면 enrichment 결과도 0이다")
		void publishFrom_Failed_ZeroWalletDeducted() {
			// given
			Payment original = createPendingPayment(Money.zero());
			Payment failed = original.fail();

			// when
			publisher.publishFrom(failed, original);

			// then
			ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
			verify(eventPublisher).publish(captor.capture());
			assertThat(captor.getValue().data().walletDeductedAmount()).isEqualTo(Money.zero());
		}
	}

	@Nested
	@DisplayName("Canceled 도메인 이벤트 변환")
	class CanceledTests {

		@Test
		@DisplayName("PaymentDomainEvent.Canceled → PaymentCanceledEvent로 변환하고 walletDeductedAmount를 enrichment한다")
		void publishFrom_Canceled_EnrichesWalletDeductedAmount() {
			// given
			Money walletDeducted = Money.of(5000);
			Payment original = createPaidPayment(walletDeducted);
			Payment canceled = original.cancel(CancelType.REFUND, "고객 요청");

			// when
			publisher.publishFrom(canceled, original);

			// then
			ArgumentCaptor<PaymentCanceledEvent> captor = ArgumentCaptor.forClass(PaymentCanceledEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentCancelData data = captor.getValue().data();
			assertThat(data.paymentId()).isEqualTo(1L);
			assertThat(data.walletDeductedAmount()).isEqualTo(walletDeducted);
			assertThat(data.cancelType()).isEqualTo(CancelType.REFUND);
			assertThat(data.reason()).isEqualTo("고객 요청");
			assertThat(data.cancelAmount()).isEqualTo(Money.of(10000));
		}
	}

	@Nested
	@DisplayName("PartialCanceled 도메인 이벤트 변환")
	class PartialCanceledTests {

		@Test
		@DisplayName("PaymentDomainEvent.PartialCanceled → PaymentCanceledEvent로 변환하고 cancelType을 REFUND로 설정한다")
		void publishFrom_PartialCanceled_SetsCancelTypeToRefund() {
			// given
			Money walletDeducted = Money.of(2000);
			Payment original = createPaidPayment(walletDeducted);
			Money cancelAmount = Money.of(3000);
			Payment partiallyCanceled = original.partialCancel("txn-cancel-001", cancelAmount, CancelType.REFUND, "부분 환불");

			// when
			publisher.publishFrom(partiallyCanceled, original);

			// then
			ArgumentCaptor<PaymentCanceledEvent> captor = ArgumentCaptor.forClass(PaymentCanceledEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentCancelData data = captor.getValue().data();
			assertThat(data.cancelAmount()).isEqualTo(cancelAmount);
			assertThat(data.walletDeductedAmount()).isEqualTo(walletDeducted);
			assertThat(data.cancelType()).isEqualTo(CancelType.REFUND);
			assertThat(data.reason()).isEqualTo("부분 환불");
			assertThat(data.transactionKey()).isEqualTo("txn-cancel-001");
		}
	}

	@Nested
	@DisplayName("CancelFailed 도메인 이벤트 변환")
	class CancelFailedTests {

		@Test
		@DisplayName("PaymentDomainEvent.CancelFailed → PaymentCancelFailedEvent로 변환한다")
		void publishFrom_CancelFailed_PublishesCancelFailedEvent() {
			// given
			Payment original = createPaidPayment(Money.zero());
			Payment cancelFailed = original.failCancel("PG 오류: 이미 취소됨");

			// when
			publisher.publishFrom(cancelFailed, original);

			// then
			ArgumentCaptor<PaymentCancelFailedEvent> captor = ArgumentCaptor.forClass(PaymentCancelFailedEvent.class);
			verify(eventPublisher).publish(captor.capture());

			PaymentCancelFailedData data = captor.getValue().data();
			assertThat(data.paymentId()).isEqualTo(1L);
			assertThat(data.orderId()).isEqualTo(100L);
			assertThat(data.errorMetadata()).isEqualTo("PG 오류: 이미 취소됨");
			assertThat(data.paymentMethod()).isEqualTo(PaymentMethod.CARD);
		}
	}
}
