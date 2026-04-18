package app.giftify.facade;

import app.giftify.facade.command.ParticipateFundingCommand;
import app.giftify.facade.command.ParticipateFundingItemCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.funding.application.FundingFacade;
import app.giftify.order.application.OrderService;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.order.domain.OrderStatus;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoreFacade 테스트")
class CoreFacadeTest {

	@Mock
	private OrderService orderService;

	@Mock
	private FundingFacade fundingFacade;

	@Mock
	private CreatePaymentService createPaymentService;

	@InjectMocks
	private CoreFacade coreFacade;

	@Nested
	@DisplayName("placeOrder 메서드")
	class PlaceOrderTests {

		@Test
		@DisplayName("주문 생성 성공 시 전체 흐름이 순서대로 실행된다")
		void placeOrder_ExecutesInOrder() {
			// given
			ParticipateFundingItemCommand itemCommand = new ParticipateFundingItemCommand(
				1L, 10L, null,  200L, Money.of(10000), OrderItemType.FUNDING_GIFT);
			ParticipateFundingCommand command = new ParticipateFundingCommand(
				100L, PaymentMethod.CARD, Money.zero(), List.of(itemCommand));

			OrderItemSnapshot orderItemSnapshot = OrderItemSnapshot.builder()
				.orderItemId(1L).orderId(1L).targetId(10L)
				.targetType(TargetType.FUNDING).orderItemType(OrderItemType.FUNDING_GIFT)
				.sellerId(200L).receiverId(200L)
				.price(Money.of(10000)).amount(Money.of(10000))
				.status(OrderItemStatus.CREATED)
				.build();

			OrderSnapshot orderSnapshot = OrderSnapshot.builder()
				.orderId(1L)
				.orderNumber("ORD-001")
				.buyerId(100L)
				.orderItemSnapshots(List.of(orderItemSnapshot))
				.totalAmount(Money.of(10000))
				.paymentMethod(PaymentMethod.CARD)
				.status(OrderStatus.CREATED)
				.createdAt(LocalDateTime.of(2026, 2, 18, 10, 0))
				.build();

			given(orderService.createOrder(any())).willReturn(orderSnapshot);

			PaymentCreatedResult paymentResult = new PaymentCreatedResult(
				1L, "ORD-001", PaymentStatus.PAID, "pay-key", "txn-key",
				LocalDateTime.of(2026, 2, 18, 10, 0));
			given(createPaymentService.create(any())).willReturn(paymentResult);

			// when
			PlaceOrderResult result = coreFacade.participateFunding(command);

			// then
			InOrder inOrder = inOrder(fundingFacade, orderService, createPaymentService);
			inOrder.verify(orderService).createOrder(any());
			inOrder.verify(createPaymentService).create(any());
			inOrder.verify(orderService).markOrderAsPaid(any());
			inOrder.verify(fundingFacade).processFundingActions(orderSnapshot);
		}

		@Test
		@DisplayName("결과로 orderId를 반환한다")
		void placeOrder_ReturnsOrderId() {
			// given
			ParticipateFundingItemCommand itemRequest = new ParticipateFundingItemCommand(
					1L, 10L, null, 200L, Money.of(10000), OrderItemType.FUNDING_GIFT);
			ParticipateFundingCommand command = new ParticipateFundingCommand(
				100L, PaymentMethod.CARD, Money.zero(), List.of(itemRequest));

			OrderItemSnapshot orderItemSnapshot = OrderItemSnapshot.builder()
				.orderItemId(1L).orderId(42L).targetId(10L)
				.targetType(TargetType.FUNDING).orderItemType(OrderItemType.FUNDING_GIFT)
				.sellerId(200L).receiverId(200L)
				.price(Money.of(10000)).amount(Money.of(10000))
				.status(OrderItemStatus.CREATED)
				.build();

			OrderSnapshot orderSnapshot = OrderSnapshot.builder()
				.orderId(42L)
				.orderNumber("ORD-042")
				.buyerId(100L)
				.orderItemSnapshots(List.of(orderItemSnapshot))
				.totalAmount(Money.of(10000))
				.paymentMethod(PaymentMethod.CARD)
				.status(OrderStatus.CREATED)
				.createdAt(LocalDateTime.of(2026, 2, 18, 10, 0))
				.build();
			given(orderService.createOrder(any())).willReturn(orderSnapshot);

			PaymentCreatedResult paymentResult = new PaymentCreatedResult(
				1L, "ORD-042", PaymentStatus.PAID, "pay-key", "txn-key",
				LocalDateTime.of(2026, 2, 18, 10, 0));
			given(createPaymentService.create(any())).willReturn(paymentResult);

			// when
			PlaceOrderResult result = coreFacade.participateFunding(command);

			// then
			assertThat(result.orderId()).isEqualTo(42L);
		}

		@Test
		@DisplayName("walletDeductAmount=0이면 CreatePaymentCommand.of()로 생성한다")
		void placeOrder_PurePG_CreatesCommandWithZeroWalletDeduct() {
			// given
			ParticipateFundingCommand command = createCommand(
				PaymentMethod.CARD, Money.zero(), Money.of(10000));
			OrderSnapshot orderSnapshot = createOrderSnapshot(
				1L, "ORD-001", PaymentMethod.CARD, Money.of(10000));
			given(orderService.createOrder(any())).willReturn(orderSnapshot);
			given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

			// when
			coreFacade.participateFunding(command);

			// then
			ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
			verify(createPaymentService).create(captor.capture());

			CreatePaymentCommand captured = captor.getValue();
			assertThat(captured.walletDeductAmount()).isEqualTo(Money.zero());
			assertThat(captured.expectedAmount()).isEqualTo(Money.of(10000));
		}

		@Test
		@DisplayName("walletDeductAmount > 0이면 CreatePaymentCommand.withWalletDeduct()로 생성한다")
		void placeOrder_Composite_CreatesCommandWithWalletDeduct() {
			// given
			Money walletDeduct = Money.of(3000);
			Money totalAmount = Money.of(10000);
			ParticipateFundingCommand command = createCommand(
				PaymentMethod.CARD, walletDeduct, totalAmount);
			OrderSnapshot orderSnapshot = createOrderSnapshot(
				1L, "ORD-001", PaymentMethod.CARD, totalAmount);
			given(orderService.createOrder(any())).willReturn(orderSnapshot);
			given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

			// when
			coreFacade.participateFunding(command);

			// then
			ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
			verify(createPaymentService).create(captor.capture());

			CreatePaymentCommand captured = captor.getValue();
			assertThat(captured.walletDeductAmount()).isEqualTo(walletDeduct);
			assertThat(captured.expectedAmount()).isEqualTo(totalAmount);
		}

		@Test
		@DisplayName("walletDeductAmount = totalAmount이면 전액 예치금 결제 Command를 생성한다")
		void placeOrder_FullWallet_CreatesCommandWithFullWalletDeduct() {
			// given
			Money totalAmount = Money.of(10000);
			ParticipateFundingCommand command = createCommand(
				PaymentMethod.CARD, totalAmount, totalAmount);
			OrderSnapshot orderSnapshot = createOrderSnapshot(
				1L, "ORD-001", PaymentMethod.CARD, totalAmount);
			given(orderService.createOrder(any())).willReturn(orderSnapshot);
			given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

			// when
			coreFacade.participateFunding(command);

			// then
			ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
			verify(createPaymentService).create(captor.capture());

			CreatePaymentCommand captured = captor.getValue();
			assertThat(captured.walletDeductAmount()).isEqualTo(totalAmount);
			assertThat(captured.expectedAmount()).isEqualTo(totalAmount);
		}

		private ParticipateFundingCommand createCommand(
				PaymentMethod method, Money walletDeductAmount, Money itemAmount) {
			ParticipateFundingItemCommand item = new ParticipateFundingItemCommand(
				1L, 10L, null, 200L, itemAmount, OrderItemType.FUNDING_GIFT);
			return new ParticipateFundingCommand(100L, method, walletDeductAmount, List.of(item));
		}

		private OrderSnapshot createOrderSnapshot(
				Long orderId, String orderNumber, PaymentMethod method, Money totalAmount) {
			OrderItemSnapshot orderItem = OrderItemSnapshot.builder()
				.orderItemId(1L).orderId(orderId).targetId(10L)
				.targetType(TargetType.FUNDING).orderItemType(OrderItemType.FUNDING_GIFT)
				.sellerId(200L).receiverId(200L)
				.price(totalAmount).amount(totalAmount)
				.status(OrderItemStatus.CREATED)
				.build();
			return OrderSnapshot.builder()
				.orderId(orderId).orderNumber(orderNumber).buyerId(100L)
				.orderItemSnapshots(List.of(orderItem))
				.totalAmount(totalAmount).paymentMethod(method)
				.status(OrderStatus.CREATED)
				.createdAt(LocalDateTime.of(2026, 2, 18, 10, 0))
				.build();
		}

		private PaymentCreatedResult defaultPaymentResult(String orderNumber) {
			return new PaymentCreatedResult(
				1L, orderNumber, PaymentStatus.PAID, "pay-key", "txn-key",
				LocalDateTime.of(2026, 2, 18, 10, 0));
		}
	}
}
