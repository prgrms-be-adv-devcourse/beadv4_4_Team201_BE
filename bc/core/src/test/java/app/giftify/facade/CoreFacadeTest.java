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
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.*;

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
				100L, PaymentMethod.CARD, List.of(itemCommand));

			FundingSnapshot fundingSnapshot = mock(FundingSnapshot.class);
			given(fundingFacade.getSnapshot(10L)).willReturn(Optional.of(fundingSnapshot));

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

			given(orderService.createOrder(any(), anyList())).willReturn(orderSnapshot);

			PaymentCreatedResult paymentResult = new PaymentCreatedResult(
				1L, "ORD-001", PaymentStatus.PAID, "pay-key", "txn-key",
				LocalDateTime.of(2026, 2, 18, 10, 0));
			given(createPaymentService.create(any())).willReturn(paymentResult);

			// when
			PlaceOrderResult result = coreFacade.participateFunding(command);

			// then
			InOrder inOrder = inOrder(fundingFacade, orderService, createPaymentService);
			inOrder.verify(fundingFacade).getSnapshot(10L);
			inOrder.verify(orderService).createOrder(any(), anyList());
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
				100L, PaymentMethod.CARD, List.of(itemRequest));

			given(fundingFacade.getSnapshot(10L)).willReturn(Optional.empty());

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
			given(orderService.createOrder(any(), anyList())).willReturn(orderSnapshot);

			PaymentCreatedResult paymentResult = new PaymentCreatedResult(
				1L, "ORD-042", PaymentStatus.PAID, "pay-key", "txn-key",
				LocalDateTime.of(2026, 2, 18, 10, 0));
			given(createPaymentService.create(any())).willReturn(paymentResult);

			// when
			PlaceOrderResult result = coreFacade.participateFunding(command);

			// then
			assertThat(result.orderId()).isEqualTo(42L);
		}
	}
}
