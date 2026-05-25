package app.giftify.usecase;

import app.giftify.order.application.OrderService;
import app.giftify.order.application.inbound.command.ParticipateFundingCommand;
import app.giftify.order.application.inbound.command.ParticipateFundingItemCommand;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.vo.PlaceOrderResult;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.order.domain.OrderStatus;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipateFundingUseCaseService 테스트")
class ParticipateFundingUseCaseServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CreatePaymentService createPaymentService;

    @InjectMocks
    private ParticipateFundingUseCaseService useCase;

    @Nested
    @DisplayName("participateFunding 메서드")
    class ParticipateFundingTests {

        @Test
        @DisplayName("정상: createOrder -> createPayment 순서로 호출되고 markOrderAsPaid 는 동기 호출되지 않는다")
        void executes_create_then_payment_without_sync_paid() {
            ParticipateFundingCommand command = createCommand(PaymentMethod.CARD, Money.zero(), Money.of(10000));
            OrderSnapshot snapshot = createOrderSnapshot(1L, "ORD-001", PaymentMethod.CARD, Money.of(10000));
            given(orderService.createOrder(any())).willReturn(snapshot);
            given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

            useCase.participateFunding(command);

            InOrder order = inOrder(orderService, createPaymentService);
            order.verify(orderService).createOrder(any(PlaceOrderCommand.class));
            order.verify(createPaymentService).create(any(CreatePaymentCommand.class));
            verify(orderService, never()).markOrderAsPaid(any());
        }

        @Test
        @DisplayName("결과: PlaceOrderResult 에 OrderSnapshot 의 orderId 가 담긴다")
        void returns_order_id_from_snapshot() {
            ParticipateFundingCommand command = createCommand(PaymentMethod.CARD, Money.zero(), Money.of(10000));
            OrderSnapshot snapshot = createOrderSnapshot(42L, "ORD-042", PaymentMethod.CARD, Money.of(10000));
            given(orderService.createOrder(any())).willReturn(snapshot);
            given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-042"));

            PlaceOrderResult result = useCase.participateFunding(command);

            assertThat(result.orderId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("walletDeductAmount = 0 → CreatePaymentCommand.of 로 생성된다")
        void pure_pg_creates_zero_wallet_command() {
            ParticipateFundingCommand command = createCommand(PaymentMethod.CARD, Money.zero(), Money.of(10000));
            OrderSnapshot snapshot = createOrderSnapshot(1L, "ORD-001", PaymentMethod.CARD, Money.of(10000));
            given(orderService.createOrder(any())).willReturn(snapshot);
            given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

            useCase.participateFunding(command);

            ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
            verify(createPaymentService).create(captor.capture());
            assertThat(captor.getValue().walletDeductAmount()).isEqualTo(Money.zero());
            assertThat(captor.getValue().expectedAmount()).isEqualTo(Money.of(10000));
            assertThat(captor.getValue().paymentType()).isEqualTo(PaymentType.FUNDING);
        }

        @Test
        @DisplayName("walletDeductAmount > 0 → CreatePaymentCommand.withWalletDeduct 로 생성된다")
        void composite_creates_wallet_deduct_command() {
            Money walletDeduct = Money.of(3000);
            Money total = Money.of(10000);
            ParticipateFundingCommand command = createCommand(PaymentMethod.CARD, walletDeduct, total);
            OrderSnapshot snapshot = createOrderSnapshot(1L, "ORD-001", PaymentMethod.CARD, total);
            given(orderService.createOrder(any())).willReturn(snapshot);
            given(createPaymentService.create(any())).willReturn(defaultPaymentResult("ORD-001"));

            useCase.participateFunding(command);

            ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
            verify(createPaymentService).create(captor.capture());
            assertThat(captor.getValue().walletDeductAmount()).isEqualTo(walletDeduct);
            assertThat(captor.getValue().expectedAmount()).isEqualTo(total);
        }

        @Test
        @DisplayName("클래스/메서드에 @Transactional 어노테이션이 없다 (자기 트랜잭션 위임)")
        void no_transactional_annotation_present() throws NoSuchMethodException {
            assertThat(ParticipateFundingUseCaseService.class.getAnnotation(Transactional.class)).isNull();
            assertThat(
                ParticipateFundingUseCaseService.class
                    .getMethod("participateFunding", ParticipateFundingCommand.class)
                    .getAnnotation(Transactional.class)
            ).isNull();
        }
    }

    private ParticipateFundingCommand createCommand(PaymentMethod method, Money walletDeductAmount, Money itemAmount) {
        ParticipateFundingItemCommand item = new ParticipateFundingItemCommand(
                1L, 10L, null, 200L, itemAmount, OrderItemType.FUNDING_GIFT);
        return new ParticipateFundingCommand(100L, method, walletDeductAmount, List.of(item));
    }

    private OrderSnapshot createOrderSnapshot(Long orderId, String orderNumber, PaymentMethod method, Money totalAmount) {
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
                .createdAt(LocalDateTime.of(2026, 5, 21, 10, 0))
                .build();
    }

    private PaymentCreatedResult defaultPaymentResult(String orderNumber) {
        return new PaymentCreatedResult(
                1L, orderNumber, PaymentStatus.PAID, "pay-key", "txn-key",
                LocalDateTime.of(2026, 5, 21, 10, 0));
    }
}
