package app.giftify.funding.adapter.inbound;

import app.giftify.funding.application.FundingAcceptUseCase;
import app.giftify.funding.application.FundingFacade;
import app.giftify.funding.application.FundingFailAcceptUseCase;
import app.giftify.funding.application.SyncFundingProductUseCase;
import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.order.application.OrderService;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.order.domain.OrderStatus;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.payment.PaymentSuccessData;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ContextConfiguration(classes = {
        FundingEventListener.class,
        FundingEventListenerTest.TestConfig.class
})
class FundingEventListenerTest {

    @TestConfiguration
    @EnableRetry
    @Import(FundingEventListener.class)
    static class TestConfig {
    }

    @Autowired
    private FundingEventListener fundingEventListener;

    @MockitoBean
    private WithdrawFundingUseCase withdrawFundingUseCase;

    @MockitoBean
    private FundingAcceptUseCase fundingAcceptUseCase;

    @MockitoBean
    private SyncFundingProductUseCase syncFundingProductUseCase;

    @MockitoBean
    private FundingFailAcceptUseCase fundingFailAcceptUseCase;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private FundingFacade fundingFacade;

    @Nested
    @DisplayName("PaymentSucceededEvent 처리 (on(PaymentSucceededEvent))")
    class OnPaymentSucceededEvent {

        private static final Long PAYMENT_ID = 42L;
        private static final Long ORDER_ID = 100L;
        private static final Long MEMBER_ID = 1L;
        private static final String ORDER_NUMBER = "ORD-100";
        private static final String TRANSACTION_KEY = "tk-1";
        private static final String PAYMENT_KEY = "pk-1";

        private PaymentSucceededEvent eventOf() {
            PaymentSuccessData data = new PaymentSuccessData(
                    PAYMENT_ID, ORDER_ID, MEMBER_ID, ORDER_NUMBER,
                    Money.of(10000L),
                    PaymentMethod.CARD, PaymentType.FUNDING,
                    PAYMENT_KEY, TRANSACTION_KEY
            );
            return PaymentSucceededEvent.create(data);
        }

        private OrderSnapshot snapshotOf() {
            OrderItemSnapshot item = OrderItemSnapshot.builder()
                    .orderItemId(1L).orderId(ORDER_ID).targetId(10L)
                    .targetType(TargetType.FUNDING).orderItemType(OrderItemType.FUNDING_GIFT)
                    .sellerId(200L).receiverId(200L)
                    .price(Money.of(10000L)).amount(Money.of(10000L))
                    .status(OrderItemStatus.CREATED)
                    .build();
            return OrderSnapshot.builder()
                    .orderId(ORDER_ID).orderNumber(ORDER_NUMBER).buyerId(MEMBER_ID)
                    .orderItemSnapshots(List.of(item))
                    .totalAmount(Money.of(10000L)).paymentMethod(PaymentMethod.CARD)
                    .status(OrderStatus.CREATED)
                    .createdAt(LocalDateTime.of(2026, 5, 21, 10, 0))
                    .build();
        }

        @Test
        @DisplayName("성공: PaymentSucceededEvent 수신 시 OrderSnapshot 조회 후 processFundingActions 가 호출된다")
        void on_PaymentSucceededEvent_calls_processFundingActions() {
            // given
            PaymentSucceededEvent event = eventOf();
            OrderSnapshot snapshot = snapshotOf();
            given(orderService.getSnapshotByOrderNumber(ORDER_NUMBER)).willReturn(snapshot);

            // when
            fundingEventListener.on(event);

            // then
            verify(orderService).getSnapshotByOrderNumber(ORDER_NUMBER);
            verify(fundingFacade).processFundingActions(snapshot);
        }

        @Test
        @DisplayName("재시도: 재시도 가능한 InfraException 발생 시 processFundingActions 가 재호출된다")
        void on_PaymentSucceededEvent_retries_on_retryable_infra_exception() {
            // given
            PaymentSucceededEvent event = eventOf();
            OrderSnapshot snapshot = snapshotOf();
            given(orderService.getSnapshotByOrderNumber(ORDER_NUMBER)).willReturn(snapshot);
            willThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT))
                    .given(fundingFacade).processFundingActions(any(OrderSnapshot.class));

            // when & then
            assertThatThrownBy(() -> fundingEventListener.on(event))
                    .isInstanceOf(InfraException.class);

            verify(fundingFacade, atLeast(2)).processFundingActions(any(OrderSnapshot.class));
        }
    }
}
