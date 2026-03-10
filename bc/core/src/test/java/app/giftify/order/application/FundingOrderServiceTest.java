package app.giftify.order.application;

import app.giftify.order.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.inbound.command.ConfirmFundingOrderCommand;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.order.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderConfirmPendingEvent;
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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingOrderServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private FundingOrderService fundingOrderService;

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
            fundingOrderService.requestCancelFundingOrder(command);

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
            fundingOrderService.requestCancelFundingOrder(command);

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
            assertThatThrownBy(() -> fundingOrderService.requestCancelFundingOrder(command))
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
            assertThatThrownBy(() -> fundingOrderService.requestCancelFundingOrder(command))
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
            assertThatCode(() -> fundingOrderService.requestCancelFundingOrder(command))
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
            assertThatCode(() -> fundingOrderService.requestCancelFundingOrder(command))
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
            assertThatCode(() -> fundingOrderService.requestCancelFundingOrder(command))
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
            assertThatCode(() -> fundingOrderService.requestCancelFundingOrder(command))
                    .doesNotThrowAnyException();

            // 예외와 무관하게 두 주문 모두에 대해 취소 요청이 시도됨
            verify(orderService, times(2)).requestCancelOrderItems(any(CancelOrderItemsCommand.class));
        }
    }

    @Nested
    @DisplayName("펀딩 수령 확정 주문 처리 (confirmOrderItemsByFunding)")
    class ConfirmOrderItemsByFunding {

        @Test
        @DisplayName("성공: 단일 주문에 속한 아이템을 확정할 때 confirmOrderItems를 정확한 인자로 1회 호출한다")
        void given_singleOrder_when_confirmOrderItemsByFunding_then_confirmOrderItemsCalledOnce() {
            // given
            Long orderId = 10L;
            Long itemId1 = 1L;
            Long itemId2 = 2L;

            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(orderId, List.of(itemId1, itemId2)));

            // when
            fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID);

            // then
            ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
            verify(orderService, times(1)).confirmOrderItems(eq(orderId), captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(itemId1, itemId2);
        }

        @Test
        @DisplayName("성공: 여러 주문에 걸쳐 있을 때 주문별로 각각 confirmOrderItems를 호출한다")
        void given_multipleOrders_when_confirmOrderItemsByFunding_then_confirmOrderItemsCalledForEachOrder() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(
                            10L, List.of(1L),
                            20L, List.of(2L)
                    ));

            // when
            fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID);

            // then
            verify(orderService, times(2)).confirmOrderItems(any(), any());
        }

        @Test
        @DisplayName("성공: 매핑 결과가 비어있으면 confirmOrderItems를 호출하지 않는다")
        void given_emptyMap_when_confirmOrderItemsByFunding_then_confirmOrderItemsNeverCalled() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of());

            // when
            fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID);

            // then
            verify(orderService, never()).confirmOrderItems(any(), any());
        }

        @Test
        @DisplayName("성공: confirmOrderItems에서 BusinessException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_businessException_when_confirmOrderItemsByFunding_then_exceptionSwallowed() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(10L, List.of(1L)));
            doThrow(new PolicyException(OrderErrorCode.INVALID_STATUS_TRANSITION))
                    .when(orderService).confirmOrderItems(any(), any());

            // when & then
            assertThatCode(() -> fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: confirmOrderItems에서 InfraException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_infraException_when_confirmOrderItemsByFunding_then_exceptionSwallowed() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(10L, List.of(1L)));
            doThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT))
                    .when(orderService).confirmOrderItems(any(), any());

            // when & then
            assertThatCode(() -> fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: confirmOrderItems에서 RuntimeException이 발생해도 예외가 삼켜지고 정상 종료된다")
        void given_runtimeException_when_confirmOrderItemsByFunding_then_exceptionSwallowed() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(10L, List.of(1L)));
            doThrow(new RuntimeException("예상치 못한 오류"))
                    .when(orderService).confirmOrderItems(any(), any());

            // when & then
            assertThatCode(() -> fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("성공: 여러 주문 중 하나에서 예외가 발생해도 나머지 주문 확정 처리가 계속된다")
        void given_exceptionOnOneOrder_when_confirmOrderItemsByFunding_then_otherOrdersStillProcessed() {
            // given
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(
                            10L, List.of(1L),
                            20L, List.of(2L)
                    ));
            // 첫 번째 호출 실패, 두 번째 호출 정상
            doThrow(new PolicyException(OrderErrorCode.INVALID_STATUS_TRANSITION))
                    .doNothing()
                    .when(orderService).confirmOrderItems(any(), any());

            // when & then
            assertThatCode(() -> fundingOrderService.confirmOrderItemsByFunding(FUNDING_ID))
                    .doesNotThrowAnyException();

            // 예외와 무관하게 두 주문 모두에 대해 확정 요청이 시도됨
            verify(orderService, times(2)).confirmOrderItems(any(), any());
        }
    }

    @Nested
    @DisplayName("펀딩 확정 주문 처리 - 커맨드 기반 (confirmOrderItemsByFunding(ConfirmFundingOrderCommand))")
    class ConfirmOrderItemsByFundingWithCommand {

        private static final Long PRODUCT_ID = 100L;

        @Test
        @DisplayName("성공: 모든 아이템이 정상 확정되면 OrderConfirmPendingEvent가 발행되고 주문 상태가 동기화된다")
        void given_allItemsConfirmed_when_confirmOrderItemsByFundingWithCommand_then_eventPublishedAndOrdersSynchronized() {
            // given
            ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(FUNDING_ID, PRODUCT_ID);
            Map<Long, List<Long>> orderIdMap = Map.of(ORDER_ID, List.of(1L, 2L));
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 2);
            ReflectionTestUtils.setField(order, "id", ORDER_ID);

            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID)).willReturn(orderIdMap);
            given(orderItemRepository.confirmOrderItems(any())).willReturn(2);
            given(orderRepository.getAllByIdInWithItems(any())).willReturn(List.of(order));

            // when
            assertThatCode(() -> fundingOrderService.confirmOrderItemsByFunding(command))
                    .doesNotThrowAnyException();

            // then
            verify(eventPublisher).publish(any(OrderConfirmPendingEvent.class));
            verify(orderItemRepository).confirmOrderItems(any());
            verify(orderRepository).getAllByIdInWithItems(any());
        }

        @Test
        @DisplayName("성공: OrderConfirmPendingEvent에 올바른 productId가 담겨 발행된다")
        void given_command_when_confirmOrderItemsByFundingWithCommand_then_publishesEventWithCorrectProductId() {
            // given
            ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(FUNDING_ID, PRODUCT_ID);
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 1);

            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(ORDER_ID, List.of(1L)));
            given(orderItemRepository.confirmOrderItems(any())).willReturn(1);
            given(orderRepository.getAllByIdInWithItems(any())).willReturn(List.of(order));

            // when
            fundingOrderService.confirmOrderItemsByFunding(command);

            // then
            ArgumentCaptor<OrderConfirmPendingEvent> captor = ArgumentCaptor.forClass(OrderConfirmPendingEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getItems()).hasSize(1);
            assertThat(captor.getValue().getItems().get(0).productId()).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("성공: 확정 대상 아이템 ID 목록이 confirmOrderItems에 올바르게 전달된다")
        void given_multipleItems_when_confirmOrderItemsByFundingWithCommand_then_allItemIdsPassedToRepository() {
            // given
            ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(FUNDING_ID, PRODUCT_ID);
            Map<Long, List<Long>> orderIdMap = Map.of(ORDER_ID, List.of(1L, 2L, 3L));
            Order order = OrderFixture.createOrderWithItems(BUYER_ID, 3);

            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID)).willReturn(orderIdMap);
            given(orderItemRepository.confirmOrderItems(any())).willReturn(3);
            given(orderRepository.getAllByIdInWithItems(any())).willReturn(List.of(order));

            // when
            fundingOrderService.confirmOrderItemsByFunding(command);

            // then
            ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
            verify(orderItemRepository).confirmOrderItems(captor.capture());
            assertThat(captor.getValue()).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("실패: 업데이트된 아이템 수가 기대치보다 적으면 DomainException(INVALID_STATUS_TRANSITION)이 발생한다")
        void given_mismatchUpdatedCount_when_confirmOrderItemsByFundingWithCommand_then_throwDomainException() {
            // given
            ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(FUNDING_ID, PRODUCT_ID);
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(ORDER_ID, List.of(1L, 2L)));
            given(orderItemRepository.confirmOrderItems(any())).willReturn(1); // 2개 중 1개만 업데이트됨

            // when & then
            assertThatThrownBy(() -> fundingOrderService.confirmOrderItemsByFunding(command))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);

            verify(orderRepository, never()).getAllByIdInWithItems(any());
        }

        @Test
        @DisplayName("실패: 업데이트된 아이템 수가 0이면 DomainException(INVALID_STATUS_TRANSITION)이 발생한다")
        void given_zeroUpdatedCount_when_confirmOrderItemsByFundingWithCommand_then_throwDomainException() {
            // given
            ConfirmFundingOrderCommand command = new ConfirmFundingOrderCommand(FUNDING_ID, PRODUCT_ID);
            given(orderItemRepository.getItemIdMapByFundingId(FUNDING_ID))
                    .willReturn(Map.of(ORDER_ID, List.of(1L)));
            given(orderItemRepository.confirmOrderItems(any())).willReturn(0);

            // when & then
            assertThatThrownBy(() -> fundingOrderService.confirmOrderItemsByFunding(command))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}