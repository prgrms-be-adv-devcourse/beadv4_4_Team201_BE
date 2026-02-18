package app.giftify.settlement.adapter.inbound.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.domain.snapshot.PaymentSnapshot;
import app.giftify.shared.domain.event.payment.PaymentConfirmedForSettlement;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementSnapshotEventListener 테스트")
class SettlementSnapshotEventListenerTest {

	@Mock
	private OrderSnapshotRepository orderSnapshotRepository;

	@Mock
	private OrderItemSnapshotRepository orderItemSnapshotRepository;

	@Mock
	private PaymentSnapshotRepository paymentSnapshotRepository;

	@Mock
	private SettlementItemService settlementItemService;

	@InjectMocks
	private SettlementSnapshotEventListener listener;

	@Test
	@DisplayName("PaymentConfirmedForSettlement 이벤트를 받으면 PaymentSnapshot을 저장한다")
	void handlePaymentConfirmedEvent_SavesPaymentSnapshot() {
		// given
		Long paymentId = 1L;
		String orderNumber = "ORD-001";
		String paymentKey = "encrypted-payment-key";
		String transactionKey = "txn-key-001";
		Money paidAmount = Money.of(10000);
		PaymentMethod method = PaymentMethod.CARD;
		LocalDateTime paidAt = LocalDateTime.of(2026, 2, 12, 14, 30);

		PaymentConfirmedForSettlement event = PaymentConfirmedForSettlement.create(
			paymentId, orderNumber, paymentKey, transactionKey, paidAmount, method, paidAt
		);

		// when
		listener.handlePaymentConfirmedEvent(event);

		// then
		ArgumentCaptor<PaymentSnapshot> captor = ArgumentCaptor.forClass(PaymentSnapshot.class);
		verify(paymentSnapshotRepository).save(captor.capture());

		PaymentSnapshot snapshot = captor.getValue();
		assertThat(snapshot.getPaymentId()).isEqualTo(paymentId);
		assertThat(snapshot.getOrderNumber()).isEqualTo(orderNumber);
		assertThat(snapshot.getPaymentKey()).isEqualTo(paymentKey);
		assertThat(snapshot.getTransactionKey()).isEqualTo(transactionKey);
		assertThat(snapshot.getPaidAt()).isEqualTo(paidAt);
		assertThat(snapshot.getPaidAmount()).isEqualTo(paidAmount);
		assertThat(snapshot.getMethod()).isEqualTo(method);
	}
}
