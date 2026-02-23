package app.giftify.order.application;

import app.giftify.order.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.order.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingOrderCancelServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private FundingOrderCancelService fundingOrderCancelService;

    private static final Long FUNDING_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long BUYER_ID = 1L;

    @Nested
    @DisplayName("펀딩 만료 주문 취소 요청 (requestCancelFundingOrder)")
    class RequestCancelFundingOrder {

        @Test
        @DisplayName("성공: 단일 주문에 속한 주문 아이템 목록에 대해 취소를 요청한다")
        void given_singleOrderItems_when_requestCancelFundingOrder_then_requestCancelOrderItemsCalledOnce() {
            // given
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2);
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            List<OrderItem> items = order.getItems();
            ReflectionTestUtils.setField(items.get(0), "id", 1L);
            ReflectionTestUtils.setField(items.get(1), "id", 2L);

            Money expiredAmount = Money.of("2000"); // 아이템 2개 × 1000원
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(items);

            // when
            fundingOrderCancelService.requestCancelFundingOrder(command);

            // then
            ArgumentCaptor<CancelOrderItemsCommand> captor = ArgumentCaptor.forClass(CancelOrderItemsCommand.class);
            verify(orderService, times(1)).requestCancelOrderItems(captor.capture());

            CancelOrderItemsCommand captured = captor.getValue();
            assertThat(captured.orderId()).isEqualTo(ORDER_ID);
            assertThat(captured.memberId()).isEqualTo(BUYER_ID);
            assertThat(captured.itemIds()).containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("성공: 여러 주문에 속한 주문 아이템이 있을 때 주문별로 각각 취소를 요청한다")
        void given_multipleOrders_when_requestCancelFundingOrder_then_requestCancelOrderItemsCalledForEachOrder() {
            // given
            Order order1 = OrderFixture.createOrderWithItems(BUYER_ID, 1);
            ReflectionTestUtils.setField(order1, "id", 10L);
            ReflectionTestUtils.setField(order1.getItems().get(0), "id", 1L);

            Order order2 = OrderFixture.createOrderWithItems(2L, 1);
            ReflectionTestUtils.setField(order2, "id", 20L);
            ReflectionTestUtils.setField(order2.getItems().get(0), "id", 2L);

            List<OrderItem> allItems = new ArrayList<>();
            allItems.addAll(order1.getItems());
            allItems.addAll(order2.getItems());

            Money expiredAmount = Money.of("2000"); // 주문 2개 × 1000원
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(allItems);

            // when
            fundingOrderCancelService.requestCancelFundingOrder(command);

            // then
            verify(orderService, times(2)).requestCancelOrderItems(any(CancelOrderItemsCommand.class));
        }

        @Test
        @DisplayName("실패: 펀딩에 매칭되는 주문 아이템이 없으면 PolicyException(ORDER_ITEM_NOT_FOUND)이 발생한다")
        void given_emptyOrderItems_when_requestCancelFundingOrder_then_throwPolicyException() {
            // given
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, Money.of("1000"));
            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ITEM_NOT_FOUND);

            verify(orderService, never()).requestCancelOrderItems(any());
        }

        @Test
        @DisplayName("실패: 펀딩 만료 금액과 주문 아이템 총합이 다르면 DomainException(CANCEL_AMOUNT_MISMATCH)이 발생한다")
        void given_amountMismatch_when_requestCancelFundingOrder_then_throwDomainException() {
            // given
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2); // 총 2000원
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            Money wrongAmount = Money.of("9999"); // 실제 합계(2000원)와 불일치
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, wrongAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(order.getItems());

            // when & then
            assertThatThrownBy(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.CANCEL_AMOUNT_MISMATCH);

            verify(orderService, never()).requestCancelOrderItems(any());
        }

        @Test
        @DisplayName("성공: orderService에서 BusinessException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_businessExceptionFromOrderService_when_requestCancelFundingOrder_then_exceptionSwallowed() {
            // given
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2);
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            Money expiredAmount = Money.of("2000");
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(order.getItems());
            given(orderService.requestCancelOrderItems(any()))
                    .willThrow(new PolicyException(OrderErrorCode.INVALID_STATUS_TRANSITION));

            // when & then - BusinessException이 외부로 전파되지 않아야 함
            assertThatCode(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: orderService에서 InfraException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_infraExceptionFromOrderService_when_requestCancelFundingOrder_then_exceptionSwallowed() {
            // given
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2);
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            Money expiredAmount = Money.of("2000");
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(order.getItems());
            given(orderService.requestCancelOrderItems(any()))
                    .willThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT));

            // when & then - InfraException이 외부로 전파되지 않아야 함
            assertThatCode(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: orderService에서 RuntimeException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_runtimeExceptionFromOrderService_when_requestCancelFundingOrder_then_exceptionSwallowed() {
            // given
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2);
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            Money expiredAmount = Money.of("2000");
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(order.getItems());
            given(orderService.requestCancelOrderItems(any()))
                    .willThrow(new RuntimeException("예상치 못한 오류"));

            // when & then - RuntimeException이 외부로 전파되지 않아야 함
            assertThatCode(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: 여러 주문 중 하나에서 예외가 발생해도 나머지 주문에 대한 취소 요청이 계속 처리된다")
        void given_exceptionOnOneOrder_when_requestCancelFundingOrder_then_otherOrdersStillProcessed() {
            // given
            Order order1 = OrderFixture.createOrderWithItems(BUYER_ID, 1);
            ReflectionTestUtils.setField(order1, "id", 10L);
            ReflectionTestUtils.setField(order1.getItems().get(0), "id", 1L);

            Order order2 = OrderFixture.createOrderWithItems(2L, 1);
            ReflectionTestUtils.setField(order2, "id", 20L);
            ReflectionTestUtils.setField(order2.getItems().get(0), "id", 2L);

            List<OrderItem> allItems = new ArrayList<>();
            allItems.addAll(order1.getItems());
            allItems.addAll(order2.getItems());

            Money expiredAmount = Money.of("2000");
            CancelFundingOrderCommand command = new CancelFundingOrderCommand(FUNDING_ID, expiredAmount);

            given(orderItemRepository.getOrderItemsWithOrderAndFindingId(FUNDING_ID)).willReturn(allItems);
            // 첫 번째 호출에서 예외 발생, 두 번째 호출은 정상 처리
            given(orderService.requestCancelOrderItems(any()))
                    .willThrow(new PolicyException(OrderErrorCode.INVALID_STATUS_TRANSITION))
                    .willReturn(null);

            // when & then
            assertThatCode(() -> fundingOrderCancelService.requestCancelFundingOrder(command))
                    .doesNotThrowAnyException();

            // 예외와 무관하게 두 주문 모두에 대해 취소 요청이 시도됨
            verify(orderService, times(2)).requestCancelOrderItems(any(CancelOrderItemsCommand.class));
        }
    }
}
