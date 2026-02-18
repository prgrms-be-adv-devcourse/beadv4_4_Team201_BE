package app.giftify.settlement.adapter.inbound.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
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

import app.giftify.settlement.application.SettlementItemService;
import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.domain.OrderItemSnapshot;
import app.giftify.settlement.domain.OrderSnapshot;
import app.giftify.settlement.domain.PaymentSnapshot;
import app.giftify.settlement.domain.errorCode.InfraErrorCode;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.DomainException;
import app.giftify.settlement.domain.exception.InfraException;
import app.giftify.shared.domain.event.funding.FundingReceivedConfirmedEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.event.payment.PaymentPaidExternalEvent;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.type.TargetType;
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

	@Nested
	@DisplayName("handlePaymentPaidExternalEvent")
	class HandlePaymentPaidExternalEventTests {

		@Test
		@DisplayName("PaymentPaidExternalEvent를 받으면 PaymentSnapshot을 저장한다")
		void handlePaymentPaidExternalEvent_SavesPaymentSnapshot() {
			// given
			Long paymentId = 1L;
			Long memberId = 100L;
			String orderNumber = "ORD-001";
			String paymentKey = "encrypted-payment-key";
			String transactionKey = "txn-key-001";
			Money paidAmount = Money.of(10000);
			PaymentMethod method = PaymentMethod.CARD;
			LocalDateTime paidAt = LocalDateTime.of(2026, 2, 12, 14, 30);

			PaymentPaidExternalEvent event = PaymentPaidExternalEvent.create(
				paymentId, orderNumber, memberId, paidAmount, PaymentType.FUNDING, method, paymentKey, transactionKey, paidAt
			);

			// when
			listener.handlePaymentPaidExternalEvent(event);

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

	@Nested
	@DisplayName("handleOrderCreatedEvent")
	class HandleOrderCreatedEventTests {

		@Test
		@DisplayName("OrderCreatedEvent를 받으면 OrderSnapshot을 저장한다")
		void handleOrderCreatedEvent_SavesOrderSnapshot() {
			// given
			Long orderId = 10L;
			String orderNumber = "ORD-010";
			LocalDateTime orderedAt = LocalDateTime.of(2026, 2, 18, 10, 0);
			OrderCreatedEvent event = new OrderCreatedEvent(orderId, orderNumber, orderedAt);

			// when
			listener.handleOrderCreatedEvent(event);

			// then
			ArgumentCaptor<OrderSnapshot> captor = ArgumentCaptor.forClass(OrderSnapshot.class);
			verify(orderSnapshotRepository).save(captor.capture());

			OrderSnapshot snapshot = captor.getValue();
			assertThat(snapshot.getOrderId()).isEqualTo(orderId);
			assertThat(snapshot.getOrderNumber()).isEqualTo(orderNumber);
			assertThat(snapshot.getOrderedAt()).isEqualTo(orderedAt);
		}
	}

	@Nested
	@DisplayName("handleOrderItemCreatedEvent")
	class HandleOrderItemCreatedEventTests {

		@Test
		@DisplayName("OrderItemCreatedEvent를 받으면 OrderItemSnapshot을 저장한다")
		void handleOrderItemCreatedEvent_SavesOrderItemSnapshot() {
			// given
			Long orderItemId = 20L;
			Long targetId = 30L;
			Long orderId = 10L;
			Long sellerId = 200L;
			Money price = Money.of(15000);
			Money amount = Money.of(15000);

			OrderItemCreatedEvent event = new OrderItemCreatedEvent(
				orderItemId, targetId, TargetType.FUNDING, OrderItemType.FUNDING_GIFT,
				orderId, sellerId, price, amount);

			// when
			listener.handleOrderItemCreatedEvent(event);

			// then
			ArgumentCaptor<OrderItemSnapshot> captor = ArgumentCaptor.forClass(OrderItemSnapshot.class);
			verify(orderItemSnapshotRepository).save(captor.capture());

			OrderItemSnapshot snapshot = captor.getValue();
			assertThat(snapshot.getOrderItemId()).isEqualTo(orderItemId);
			assertThat(snapshot.getOrderId()).isEqualTo(orderId);
			assertThat(snapshot.getTargetId()).isEqualTo(targetId);
			assertThat(snapshot.getTargetType()).isEqualTo(TargetType.FUNDING);
			assertThat(snapshot.getOrderItemType()).isEqualTo(OrderItemType.FUNDING_GIFT);
			assertThat(snapshot.getSellerId()).isEqualTo(sellerId);
			assertThat(snapshot.getPrice()).isEqualTo(price);
			assertThat(snapshot.getAmount()).isEqualTo(amount);
		}
	}

	@Nested
	@DisplayName("handleFundingReceivedConfirmedEvent")
	class HandleFundingReceivedConfirmedEventTests {

		@Test
		@DisplayName("정상 처리 시 initializeSettlementItem을 호출한다")
		void handleFundingReceivedConfirmedEvent_CallsInitializeSettlementItem() {
			// given
			Long fundingId = 50L;
			LocalDateTime confirmedAt = LocalDateTime.of(2026, 2, 18, 15, 0);
			FundingReceivedConfirmedEvent event = new FundingReceivedConfirmedEvent(fundingId, confirmedAt);

			// when
			listener.handleFundingReceivedConfirmedEvent(event);

			// then
			ArgumentCaptor<InitializeSettlementItemCommand> captor =
				ArgumentCaptor.forClass(InitializeSettlementItemCommand.class);
			verify(settlementItemService).initializeSettlementItem(captor.capture());

			InitializeSettlementItemCommand command = captor.getValue();
			assertThat(command.fundingId()).isEqualTo(fundingId);
			assertThat(command.confirmedAt()).isEqualTo(confirmedAt);
		}

		@Test
		@DisplayName("재시도 대상 SettlementException 발생 시 예외를 삼킨다")
		void handleFundingReceivedConfirmedEvent_SwallowsRetryableException() {
			// given
			FundingReceivedConfirmedEvent event = new FundingReceivedConfirmedEvent(
				50L, LocalDateTime.of(2026, 2, 18, 15, 0));
			willThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT))
				.given(settlementItemService).initializeSettlementItem(any());

			// when & then
			assertThatCode(() -> listener.handleFundingReceivedConfirmedEvent(event))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("비재시도 SettlementException 발생 시 예외를 삼킨다")
		void handleFundingReceivedConfirmedEvent_SwallowsNonRetryableException() {
			// given
			FundingReceivedConfirmedEvent event = new FundingReceivedConfirmedEvent(
				50L, LocalDateTime.of(2026, 2, 18, 15, 0));
			willThrow(new DomainException(SettlementErrorCode.DUPLICATE_SETTLEMENT_ITEM))
				.given(settlementItemService).initializeSettlementItem(any());

			// when & then
			assertThatCode(() -> listener.handleFundingReceivedConfirmedEvent(event))
				.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("예상치 못한 예외 발생 시 상위로 전파한다")
		void handleFundingReceivedConfirmedEvent_PropagatesUnexpectedException() {
			// given
			FundingReceivedConfirmedEvent event = new FundingReceivedConfirmedEvent(
				50L, LocalDateTime.of(2026, 2, 18, 15, 0));
			willThrow(new RuntimeException("unexpected error"))
				.given(settlementItemService).initializeSettlementItem(any());

			// when & then
			assertThatThrownBy(() -> listener.handleFundingReceivedConfirmedEvent(event))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("unexpected error");
		}
	}
}
