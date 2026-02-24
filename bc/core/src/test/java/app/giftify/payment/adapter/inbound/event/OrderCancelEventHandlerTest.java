package app.giftify.payment.adapter.inbound.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.payment.application.outbound.CancelRepository;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Cancel;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCancelEventHandler 테스트")
class OrderCancelEventHandlerTest {

	@Mock
	PaymentRepository paymentRepository;

	@Mock
	CancelRepository cancelRepository;

	@Mock
	EventPublisher eventPublisher;

	@InjectMocks
	OrderCancelEventHandler handler;

	private Payment createPaidFundingPayment(Money paidAmount) {
		OrderItemSnapshot item = new OrderItemSnapshot(100L, paidAmount, 200L);
		return Payment.builder()
			.id(1L)
			.orderId(100L)
			.orderNumber("ORD-001")
			.memberId(10L)
			.type(PaymentType.FUNDING)
			.method(PaymentMethod.DEPOSIT)
			.originAmount(paidAmount)
			.paidAmount(paidAmount)
			.refundedAmount(Money.zero())
			.orderItems(List.of(item))
			.status(PaymentStatus.PAID)
			.paymentKey("payment-key-123")
			.lastTransactionKey("txn-001")
			.approveCode("approve-001")
			.paidAt(LocalDateTime.now())
			.createdAt(LocalDateTime.now())
			.build();
	}

	@Test
	@DisplayName("FUNDING 부분 취소 → PARTIALLY_CANCELED, Cancel 저장, PaymentCanceledEvent 발행")
	void handle_FundingPartialCancel_Success() {
		Payment payment = createPaidFundingPayment(Money.of(10000));
		OrderCancelRequestedEvent event = new OrderCancelRequestedEvent(
			100L, "ORD-001", 1L, "txn-001", Money.of(3000)
		);

		when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

		handler.handle(event);

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_CANCELED);
		assertThat(payment.getRefundedAmount()).isEqualTo(Money.of(3000));

		ArgumentCaptor<Cancel> cancelCaptor = ArgumentCaptor.forClass(Cancel.class);
		verify(cancelRepository).save(cancelCaptor.capture());
		Cancel savedCancel = cancelCaptor.getValue();
		assertThat(savedCancel.getPaymentId()).isEqualTo(1L);
		assertThat(savedCancel.getCancelAmount()).isEqualTo(Money.of(3000));
		assertThat(savedCancel.getCancelReason()).isEqualTo("주문 취소");
		assertThat(savedCancel.getTransactionKey()).startsWith("internal-");

		verify(paymentRepository).save(payment);
		verify(eventPublisher).publish(any(PaymentCanceledEvent.class));
	}

	@Test
	@DisplayName("FUNDING 전체 취소 → CANCELED")
	void handle_FundingFullCancel_Success() {
		Payment payment = createPaidFundingPayment(Money.of(10000));
		OrderCancelRequestedEvent event = new OrderCancelRequestedEvent(
			100L, "ORD-001", 1L, "txn-001", Money.of(10000)
		);

		when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

		handler.handle(event);

		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
		assertThat(payment.getRefundedAmount()).isEqualTo(Money.of(10000));

		verify(cancelRepository).save(any(Cancel.class));
		verify(paymentRepository).save(payment);
		verify(eventPublisher).publish(any(PaymentCanceledEvent.class));
	}
}
