package app.giftify.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentFailService 테스트")
class PaymentFailServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private PaymentModuleEventPublisher moduleEventPublisher;

	@InjectMocks
	private PaymentFailService paymentFailService;

	private Payment createPendingPayment(Long paymentId, PaymentMethod method) {
		return Payment.builder()
			.id(paymentId)
			.orderId(123L)
			.orderNumber("ORD-001")
			.memberId(100L)
			.type(PaymentType.FUNDING)
			.method(method)
			.originAmount(Money.of(10000))
			.paidAmount(Money.of(10000))
			.walletDeductedAmount(method.isWalletPayment() ? Money.of(10000) : Money.zero())
			.status(PaymentStatus.PENDING)
			.build();
	}

	@Nested
	@DisplayName("fail 메서드")
	class FailTests {

		@Test
		@DisplayName("PENDING 상태 결제를 FAILED로 변경하고 저장한다")
		void fail_ChangeStatusToFailed_AndSaves() {
			// given
			Payment payment = createPendingPayment(1L, PaymentMethod.CARD);
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			paymentFailService.fail(payment);

			// then
			ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(captor.capture());
			assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
		}

		@Test
		@DisplayName("실패 처리 후 모듈 이벤트를 발행한다")
		void fail_PublishesModuleEvent() {
			// given
			Payment payment = createPendingPayment(1L, PaymentMethod.CARD);
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			paymentFailService.fail(payment);

			// then
			verify(moduleEventPublisher).publishFrom(any(Payment.class), any(Payment.class));
		}

		@Test
		@DisplayName("DEPOSIT 결제 실패 시에도 모듈 이벤트를 발행한다")
		void fail_WalletPayment_PublishesModuleEvent() {
			// given
			Payment payment = createPendingPayment(2L, PaymentMethod.DEPOSIT);
			given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

			// when
			paymentFailService.fail(payment);

			// then
			ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
			verify(paymentRepository).save(captor.capture());
			assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
			verify(moduleEventPublisher).publishFrom(any(Payment.class), any(Payment.class));
		}

		@Test
		@DisplayName("PAID 상태 결제를 fail 호출하면 예외가 발생한다")
		void fail_WhenNotPending_ThrowsException() {
			// given
			Payment paidPayment = Payment.builder()
				.id(1L)
				.orderId(123L)
				.orderNumber("ORD-001")
				.memberId(100L)
				.type(PaymentType.FUNDING)
				.method(PaymentMethod.CARD)
				.originAmount(Money.of(10000))
				.paidAmount(Money.of(10000))
				.status(PaymentStatus.PAID)
				.paidAt(LocalDateTime.now())
				.build();

			// when & then
			assertThatThrownBy(() -> paymentFailService.fail(paidPayment))
				.isInstanceOf(PaymentException.class);

			verify(paymentRepository, never()).save(any());
			verify(moduleEventPublisher, never()).publishFrom(any(Payment.class), any(Payment.class));
		}
	}
}
