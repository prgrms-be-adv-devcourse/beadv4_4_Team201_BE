package app.giftify.order.application;

import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.command.PlaceOrderItemCommand;
import app.giftify.order.application.inbound.vo.OrderSummary;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.product.domain.port.ProductSnapshotPort;
import app.giftify.order.domain.*;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.order.domain.fixture.OrderFixture;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.api.exception.InfraErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import app.giftify.support.common.api.exception.PolicyException;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.order.domain.event.OrderCancelRequestedEvent;
import app.giftify.order.domain.event.OrderCanceledEvent;
import app.giftify.funding.domain.port.FundingQueryPort;
import app.giftify.funding.domain.type.FundingStatus;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.order.domain.type.TargetType;
import app.giftify.funding.domain.vo.FundingInfo;
import app.giftify.support.common.money.Money;
import app.giftify.product.domain.vo.ProductSnapshot;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductSnapshotPort productPort;
    @Mock
    private FundingQueryPort fundingQueryPort;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private TargetTypeResolver targetTypeResolver;
    @Mock
    private TargetIdResolver targetIdResolver;

    @InjectMocks
    private OrderService orderService;

    private final Long wishlistItemId = 100L;
    private final Long productId = 500L;
    private final Map<Long, ProductSnapshot> productSnapshots = Map.of(
            productId,
            new ProductSnapshot(
                    productId,
                    200000,
                    200L,
                    true
            )
    );

    private final List<Long> wishlistItemIds = List.of(wishlistItemId);
    private final List<Long> productIds = List.of(productId);

    @Nested
    @DisplayName("주문 생성 처리 (placeOrder)")
    class PlaceOrder {
        @Test
        @DisplayName("일반 구매 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
        void placeOrder_success_normalProduct() {
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(targetTypeResolver.resolve(any(), any())).willReturn(TargetType.DIRECT_PURCHASE);
            given(targetIdResolver.resolve(any(), any(), any())).willReturn(productId);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            // when
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(null, OrderItemType.NORMAL_ORDER);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            OrderSnapshot result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderItemSnapshots()).hasSize(1);
            assertThat(result.orderItemSnapshots().getFirst().targetId()).isEqualTo(productId);

            // 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
            verify(productPort, times(1)).getProductSnapshots(productIds);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("일반 선물 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
        void placeOrder_success_normalGift() {
            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(Map.of());
            given(targetTypeResolver.resolve(any(), any())).willReturn(TargetType.DIRECT_GIFT);
            given(targetIdResolver.resolve(any(), any(), any())).willReturn(wishlistItemId);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            // when
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(null, OrderItemType.NORMAL_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            OrderSnapshot result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderItemSnapshots()).hasSize(1);
            assertThat(result.orderItemSnapshots().getFirst().targetId()).isEqualTo(wishlistItemId);

            // 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
            verify(productPort, times(1)).getProductSnapshots(productIds);
            verify(fundingQueryPort, times(1)).findFundingInfoByWishlistItemIds(wishlistItemIds);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("펀딩 중인 상품 일반 선물 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
        void placeOrder_success_normalGift_directGiftOnFunding() {
            final Long fundingId = 101L;

            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(Map.of(
                    wishlistItemId,
                    new FundingInfo(
                            fundingId,
                            FundingStatus.IN_PROGRESS,
                            180000,
                            20000
                    )
            ));
            given(targetTypeResolver.resolve(any(), any())).willReturn(TargetType.DIRECT_GIFT_ON_FUNDING);
            given(targetIdResolver.resolve(any(), any(), any())).willReturn(wishlistItemId);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            // when
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(fundingId, OrderItemType.NORMAL_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            OrderSnapshot result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderItemSnapshots()).hasSize(1);
            assertThat(result.orderItemSnapshots().getFirst().targetId()).isEqualTo(wishlistItemId);

            // 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
            verify(productPort, times(1)).getProductSnapshots(productIds);
            verify(fundingQueryPort, times(1)).findFundingInfoByWishlistItemIds(wishlistItemIds);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("첫 기여 펀딩 선물 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
        void placeOrder_success_fundingGift_fundingPending() {
            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(Map.of());
            given(targetTypeResolver.resolve(any(), any())).willReturn(TargetType.FUNDING_PENDING);
            given(targetIdResolver.resolve(any(), any(), any())).willReturn(wishlistItemId);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            // when
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(null, OrderItemType.FUNDING_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            OrderSnapshot result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderItemSnapshots()).hasSize(1);
            assertThat(result.orderItemSnapshots().getFirst().targetId()).isEqualTo(wishlistItemId);

            // 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
            verify(productPort, times(1)).getProductSnapshots(productIds);
            verify(fundingQueryPort, times(1)).findFundingInfoByWishlistItemIds(wishlistItemIds);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("기존 펀딩 선물 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
        void placeOrder_success_fundingGift_funding() {
            final Long fundingId = 101L;

            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(Map.of(
                    wishlistItemId,
                    new FundingInfo(
                            fundingId,
                            FundingStatus.IN_PROGRESS,
                            180000,
                            20000
                    )
            ));
            given(targetTypeResolver.resolve(any(), any())).willReturn(TargetType.FUNDING);
            given(targetIdResolver.resolve(any(), any(), any())).willReturn(fundingId);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            // when
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(fundingId, OrderItemType.FUNDING_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            OrderSnapshot result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderItemSnapshots()).hasSize(1);
            assertThat(result.orderItemSnapshots().getFirst().targetId()).isEqualTo(fundingId);

            // 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
            verify(productPort, times(1)).getProductSnapshots(productIds);
            verify(fundingQueryPort, times(1)).findFundingInfoByWishlistItemIds(wishlistItemIds);
            verify(orderRepository, times(1)).save(any(Order.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("실패 - 상품 정보가 없을 경우 예외가 발생한다")
        void placeOrder_fail_productSnapshotNotFound() {
            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(null);

            // when & then
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(null, OrderItemType.NORMAL_ORDER);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.SNAPSHOTS_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 펀딩 정보가 없을 경우 예외가 발생한다")
        void placeOrder_fail_fundingInfoNotFound() {
            // given
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(null);

            // when & then
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(1L, OrderItemType.FUNDING_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.SNAPSHOTS_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 진행 중인 펀딩 선물 주문이면서 펀딩 정보가 없을 경우 예외가 발생한다.")
        void placeOrder_fail_fundingInfoNotFound_onFundingGift() {
            // given
            given(productPort.getProductSnapshots(productIds)).willReturn(productSnapshots);
            given(fundingQueryPort.findFundingInfoByWishlistItemIds(wishlistItemIds)).willReturn(Map.of());
            given(targetTypeResolver.resolve(any(), any())).willThrow(new PolicyException(OrderErrorCode.UNSUPPORTED_ORDER_COMBINATION));

            // when & then
            PlaceOrderItemCommand itemCommand = createPlaceOrderItemCommand(1L, OrderItemType.FUNDING_GIFT);
            PlaceOrderCommand command = createPlaceOrderCommand(itemCommand);
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(PolicyException.class)
                    .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNSUPPORTED_ORDER_COMBINATION);
        }
    }

    private @NonNull PlaceOrderCommand createPlaceOrderCommand(PlaceOrderItemCommand itemCommand) {
        return new PlaceOrderCommand(
                1L,
                PaymentMethod.DEPOSIT,
                List.of(itemCommand)
        );
    }

    private @NonNull PlaceOrderItemCommand createPlaceOrderItemCommand(Long fundingId, OrderItemType orderItemType) {
        return new PlaceOrderItemCommand(
                productId,
                wishlistItemId,
                fundingId,
                2002L,
                Money.of("15000"),
                orderItemType
        );
    }

    @Test
    void getOrders_shouldReturnMappedOrderViews() {
        // Given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 2);

        // 샘플 Order 엔티티 생성
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        MockedStatic<OrderSummary> orderViewMock = Mockito.mockStatic(OrderSummary.class);

        // OrderView 반환용 가짜 OrderView
        OrderSummary view1 = new OrderSummary(
                1L,
                "order_number1",
                1L,
                Money.of("10000"),
                OrderStatus.CREATED,
                PaymentMethod.DEPOSIT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        OrderSummary view2 = new OrderSummary(
                2L,
                "order_number2",
                2L,
                Money.of("20000"),
                OrderStatus.CREATED,
                PaymentMethod.DEPOSIT,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // OrderView.of(Order) 가 호출될 때도 필요하면 stubbing
        orderViewMock.when(() -> OrderSummary.of(order1)).thenReturn(view1);
        orderViewMock.when(() -> OrderSummary.of(order2)).thenReturn(view2);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

        when(orderRepository.getByBuyerId(memberId, pageable)).thenReturn(orderPage);

        // When
        Page<OrderSummary> result = orderService.getOrders(memberId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent().get(0)).isEqualTo(view1);
        assertThat(result.getContent().get(1)).isEqualTo(view2);

        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);

        verify(orderRepository, times(1)).getByBuyerId(memberId, pageable);
    }

    @Test
    @DisplayName("getOrders: 주문 없음 → 빈 Page 반환")
    void getOrders_shouldReturnEmptyPage_whenNoOrders() {
        // Given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 1);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.getByBuyerId(memberId, pageable)).thenReturn(emptyPage);

        // When
        Page<OrderSummary> result = orderService.getOrders(memberId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty(); // 주문이 없으면 빈 리스트
        assertThat(result.getTotalElements()).isZero();
        verify(orderRepository, times(1)).getByBuyerId(memberId, pageable);
    }

    @Nested
    @DisplayName("주문 전체 취소 (requestCancelOrder)")
    class CancelOrder {

        private final Long memberId = 1L;
        private final Long orderId = 10L;

        @Test
        @DisplayName("성공: 결제 완료된 주문 취소 요청 시 CANCELING 상태로 변경하고 취소 요청 이벤트를 발행한다")
        void given_paidOrder_when_requestCancelOrder_then_returnCancelPending() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.PAID);
            ReflectionTestUtils.setField(order, "id", orderId);

            Money expectedCancelAmount = Money.of("2000"); // 아이템 2개 × 1000원

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            ResultCode result = orderService.requestCancelOrder(memberId, orderId);

            // then
            assertThat(result).isEqualTo(ResultCode.ACCEPTED);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELING);

            ArgumentCaptor<OrderCancelRequestedEvent> captor = ArgumentCaptor.forClass(OrderCancelRequestedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getCancelAmount()).isEqualTo(expectedCancelAmount);
        }

        @Test
        @DisplayName("성공: 결제 전(CREATED) 주문 취소 시 즉시 CANCELED로 변경하고 이벤트를 발행하지 않는다")
        void given_createdOrder_when_requestCancelOrder_then_returnCancelSuccess() {
            // given
            Order order = OrderFixture.createOrderWithItems(memberId, 2);
            ReflectionTestUtils.setField(order, "id", orderId);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            ResultCode result = orderService.requestCancelOrder(memberId, orderId);

            // then
            assertThat(result).isEqualTo(ResultCode.SUCCESS);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("멱등: 이미 취소된 주문에 재요청 시 ALREADY_CANCELED를 반환하고 추가 처리를 하지 않는다")
        void given_canceledOrder_when_requestCanceledOrder_then_returnAlreadyCanceled() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            ResultCode result = orderService.requestCancelOrder(memberId, orderId);

            // then
            assertThat(result).isEqualTo(ResultCode.ALREADY_PROCESSED);
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("멱등: 취소 처리 중인 주문에 재요청 시 IN_PROGRESS를 반환하고 추가 처리를 하지 않는다")
        void given_cancelPendingOrder_when_requestCanceledOrder_then_returnInProgress() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            ResultCode result = orderService.requestCancelOrder(memberId, orderId);

            // then
            assertThat(result).isEqualTo(ResultCode.IN_PROGRESS);
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("실패: 구매 확정된 주문(CONFIRMED)에 취소 요청 시 INVALID_STATUS_TRANSITION 예외가 발생한다")
        void given_confirmedOrder_when_requestCancelOrder_then_throwInvalidStatusCancel() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CONFIRMED);
            ReflectionTestUtils.setField(order, "id", orderId);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when & then
            assertThatThrownBy(() -> orderService.requestCancelOrder(memberId, orderId))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);

            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문 ID로 요청 시 ORDER_NOT_FOUND 예외가 발생한다")
        void given_nonExistentOrderId_when_cancelOrder_then_throwOrderNotFound() {
            // given
            given(orderRepository.getByIdWithItemsAndLock(orderId))
                    .willThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.requestCancelOrder(memberId, orderId))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);

            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("실패: 주문 소유자가 아닌 회원이 취소 요청 시 ORDER_OWNER_MISMATCH 예외가 발생한다")
        void given_differentMember_when_cancelOrder_then_throwOwnerMismatch() {
            // given
            Long anotherMemberId = 999L;
            Order order = OrderFixture.createOrderWithItems(memberId, 2); // buyerId = 1L

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when & then
            assertThatThrownBy(() -> orderService.requestCancelOrder(anotherMemberId, orderId))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_OWNER_MISMATCH);

            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("실패: DB 락 획득 타임아웃 시 InfraException이 전파된다 (@Retryable 재시도 대상)")
        void given_dbLockTimeout_when_cancelOrder_then_infraExceptionPropagates() {
            // given
            given(orderRepository.getByIdWithItemsAndLock(orderId))
                    .willThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT, "비관적 락 획득 실패"));

            // when & then
            assertThatThrownBy(() -> orderService.requestCancelOrder(memberId, orderId))
                    .isInstanceOf(InfraException.class)
                    .extracting("errorCode")
                    .isEqualTo(InfraErrorCode.DB_LOCK_TIMEOUT);

            // InfraException이 삼켜지지 않고 전파되어야 @Retryable이 재시도할 수 있음
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("실패: 아이템 조회 중 일시적 DB 오류 발생 시 InfraException이 전파된다")
        void given_dbErrorOnItemQuery_when_cancelOrder_then_infraExceptionPropagates() {
            // given
            Order order = OrderFixture.createOrderWithItems(memberId, 2);
            ReflectionTestUtils.setField(order, "id", orderId);

            given(orderRepository.getByIdWithItemsAndLock(orderId))
                    .willThrow(new InfraException(InfraErrorCode.DB_TEMPORARY_ERROR, "일시적 DB 오류"));


            // when & then
            assertThatThrownBy(() -> orderService.requestCancelOrder(memberId, orderId))
                    .isInstanceOf(InfraException.class)
                    .extracting("errorCode")
                    .isEqualTo(InfraErrorCode.DB_TEMPORARY_ERROR);

            verify(eventPublisher, never()).publish(any());
        }
    }

    @Nested
    @DisplayName("주문 취소 확정 (completeCancel)")
    class CompleteCancel {

        private final Long orderId = 10L;

        @Test
        @DisplayName("성공: CANCELING 상태 주문의 취소를 확정하여 CANCELED로 변경하고 OrderCanceledEvent를 발행한다")
        void given_cancelPendingOrder_when_completeCancel_then_statusCanceled() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);
            ReflectionTestUtils.setField(order, "id", orderId);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            orderService.completeCancel(orderId);

            // then
            verify(orderRepository).getByIdWithItemsAndLock(orderId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);

            ArgumentCaptor<OrderCanceledEvent> captor = ArgumentCaptor.forClass(OrderCanceledEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문 ID로 요청 시 ORDER_NOT_FOUND 예외가 발생한다")
        void given_nonExistentOrderId_when_completeCancel_then_throwOrderNotFound() {
            // given
            given(orderRepository.getByIdWithItemsAndLock(orderId))
                    .willThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.completeCancel(orderId))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("주문 취소 실패 처리 (failCancel)")
    class FailCancel {

        private final Long orderId = 10L;

        @Test
        @DisplayName("성공: 주문 취소 실패 처리 시 주문 아이템 복구 후 상태를 복구한다")
        void given_cancelPendingOrder_when_failCancel_then_statusRestored() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);
            ReflectionTestUtils.setField(order, "id", orderId);

            given(orderRepository.getByIdWithItemsAndLock(orderId)).willReturn(order);

            // when
            orderService.failCancel(orderId);

            // then
            verify(orderRepository).getByIdWithItemsAndLock(orderId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문 ID로 요청 시 ORDER_NOT_FOUND 예외가 발생한다")
        void given_nonExistentOrderId_when_failCancel_then_throwOrderNotFound() {
            // given
            given(orderRepository.getByIdWithItemsAndLock(orderId))
                    .willThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.failCancel(orderId))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("결제 완료 처리 (markOrderAsPaid)")
    class MarkOrderAsPaid {

        @Test
        @DisplayName("성공: 주문을 조회하여 결제 완료 상태로 변경하고 주문 취소 완료 이벤트를 발행한다")
        void success() {
            // given
            Order order = spy(OrderFixture.createOrderWithItems(1L, 2)); // 엔티티 메서드 호출 확인을 위해 spy 사용

            String orderNumber = "orderNumber";

            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(
                    "orderNumber",
                    1L,
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            given(orderRepository.getByOrderNumber(orderNumber)).willReturn(order);

            // when
            orderService.markOrderAsPaid(command);

            // then
            // 1. 레포지토리 조회가 정확한 주문번호로 이루어졌는지 확인
            verify(orderRepository).getByOrderNumber(orderNumber);

            // 2. 엔티티의 toPaid 메서드가 스냅샷의 데이터로 호출되었는지 확인
            verify(order).paid(command.paymentId(), command.lastTransactionKey());

            // 3. 실제 상태가 변했는지 확인 (더티 체킹에 의해 반영될 상태)
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문번호일 경우 예외가 발생한다")
        void fail_not_found() {
            // given
            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(
                    "orderNumber",
                    1L,
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            String orderNumber = "orderNumber";

            given(orderRepository.getByOrderNumber(orderNumber))
                    .willThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.markOrderAsPaid(command))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);

            // 조회가 실패했으므로 엔티티 로직은 실행되지 않아야 함을 검증
            verify(orderRepository, never()).save(any());// save를 안 쓰는 구조라도 상호작용 없음 확인
        }

        @Test
        @DisplayName("실패: 취소된 주문 등 결제 불가능한 상태일 경우 PolicyException이 발생한다")
        void fail_invalid_status() {
            // given
            // 이미 취소된 주문 준비
            Order cancelledOrder = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);
            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(
                    "orderNumber",
                    1L,
                    "TX_KEY_456",
                    LocalDateTime.now()
            );

            String orderNumber = "orderNumber";

            given(orderRepository.getByOrderNumber(orderNumber)).willReturn(cancelledOrder);

            // when & then
            assertThatThrownBy(() -> orderService.markOrderAsPaid(command))
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);

            // 상태가 변하지 않았는지 확인
            assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("주문 금액 집계 (calculateTotalAmounts)")
    class CalculateTotalAmounts {

        @Test
        @DisplayName("성공: 빈 리스트 입력 시 repository 호출 없이 빈 Map을 반환한다")
        void given_emptyOrderIds_when_calculateTotalAmounts_then_returnEmptyMap() {
            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(List.of());

            // then
            assertThat(result).isEmpty();
            verify(orderRepository, never()).getAllByIdInWithItems(any());
        }

        @Test
        @DisplayName("성공: PAID 상태 아이템이 있는 주문의 정산 가능 금액을 반환한다")
        void given_paidItems_when_calculateTotalAmounts_then_returnPayableAmount() {
            // given
            Long orderId1 = 1L;
            Long orderId2 = 2L;

            Order order1 = OrderFixture.createOrderWithItems(1L, 2); // 아이템 2개, 각 1000원
            Order order2 = OrderFixture.createOrderWithItems(2L, 3); // 아이템 3개, 각 1000원
            ReflectionTestUtils.setField(order1, "id", orderId1);
            ReflectionTestUtils.setField(order2, "id", orderId2);
            order1.getItems().forEach(item -> ReflectionTestUtils.setField(item, "status", OrderItemStatus.PAID));
            order2.getItems().forEach(item -> ReflectionTestUtils.setField(item, "status", OrderItemStatus.PAID));

            List<Long> orderIds = List.of(orderId1, orderId2);
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order1, order2));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(orderId1)).isEqualTo(Money.of("2000"));
            assertThat(result.get(orderId2)).isEqualTo(Money.of("3000"));
        }

        @Test
        @DisplayName("성공: CREATED 상태 아이템은 정산 대상이 아니므로 payableAmount가 0원이다")
        void given_createdItems_when_calculateTotalAmounts_then_returnZeroPayableAmount() {
            // given
            Long orderId = 1L;
            Order order = OrderFixture.createOrderWithItems(1L, 2); // CREATED 상태 (기본값)
            ReflectionTestUtils.setField(order, "id", orderId);

            List<Long> orderIds = List.of(orderId);
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(orderId)).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("성공: PAID/CANCELED 혼합 시 PAID 아이템 금액만 합산된다")
        void given_mixedPaidAndCanceledItems_when_calculateTotalAmounts_then_returnOnlyPaidAmount() {
            // given
            Long orderId = 1L;
            Order order = OrderFixture.createOrderWithItems(1L, 2); // 아이템 2개, 각 1000원
            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(order.getItems().get(0), "status", OrderItemStatus.PAID);
            ReflectionTestUtils.setField(order.getItems().get(1), "status", OrderItemStatus.CANCELED);

            List<Long> orderIds = List.of(orderId);
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then
            assertThat(result.get(orderId)).isEqualTo(Money.of("1000"));
        }

        @Test
        @DisplayName("성공: 특정 주문에서 BusinessException 발생 시 해당 주문만 제외하고 나머지는 정상 반환한다")
        void given_businessExceptionOnOneOrder_when_calculateTotalAmounts_then_excludeFailedOrder() {
            // given
            Long orderId1 = 1L;
            Long orderId2 = 2L;
            List<Long> orderIds = List.of(orderId1, orderId2);

            Order order1 = spy(OrderFixture.createOrderWithItems(1L, 2));
            Order order2 = OrderFixture.createOrderWithItems(2L, 2);
            ReflectionTestUtils.setField(order1, "id", orderId1);
            ReflectionTestUtils.setField(order2, "id", orderId2);
            order2.getItems().forEach(item -> ReflectionTestUtils.setField(item, "status", OrderItemStatus.PAID));

            doThrow(new PolicyException(OrderErrorCode.ORDER_NOT_FOUND)).when(order1).getPayableAmount();
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order1, order2));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then - order1은 제외, order2만 포함
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(orderId2);
            assertThat(result).doesNotContainKey(orderId1);
        }

        @Test
        @DisplayName("성공: 특정 주문에서 retryable=false InfraException 발생 시 해당 주문만 제외하고 나머지는 정상 반환한다")
        void given_nonRetryableInfraExceptionOnOneOrder_when_calculateTotalAmounts_then_excludeFailedOrder() {
            // given
            Long orderId1 = 1L;
            Long orderId2 = 2L;
            List<Long> orderIds = List.of(orderId1, orderId2);

            Order order1 = spy(OrderFixture.createOrderWithItems(1L, 2));
            Order order2 = OrderFixture.createOrderWithItems(2L, 2);
            ReflectionTestUtils.setField(order1, "id", orderId1);
            ReflectionTestUtils.setField(order2, "id", orderId2);
            order2.getItems().forEach(item -> ReflectionTestUtils.setField(item, "status", OrderItemStatus.PAID));

            doThrow(new InfraException(InfraErrorCode.DB_CONSTRAINT_VIOLATION)).when(order1).getPayableAmount(); // retryable=false
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order1, order2));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then - order1은 제외, order2만 포함
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(orderId2);
            assertThat(result).doesNotContainKey(orderId1);
        }

        @Test
        @DisplayName("실패: 특정 주문에서 retryable=true InfraException 발생 시 예외가 외부로 전파된다")
        void given_retryableInfraExceptionOnOneOrder_when_calculateTotalAmounts_then_throwInfraException() {
            // given
            Long orderId = 1L;
            List<Long> orderIds = List.of(orderId);

            Order order = spy(OrderFixture.createOrderWithItems(1L, 2));
            ReflectionTestUtils.setField(order, "id", orderId);

            doThrow(new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT)).when(order).getPayableAmount(); // retryable=true
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.calculateTotalAmounts(orderIds))
                    .isInstanceOf(InfraException.class)
                    .extracting("errorCode")
                    .isEqualTo(InfraErrorCode.DB_LOCK_TIMEOUT);
        }

        @Test
        @DisplayName("성공: 특정 주문에서 RuntimeException 발생 시 해당 주문만 제외하고 나머지는 정상 반환한다")
        void given_runtimeExceptionOnOneOrder_when_calculateTotalAmounts_then_excludeFailedOrder() {
            // given
            Long orderId1 = 1L;
            Long orderId2 = 2L;
            List<Long> orderIds = List.of(orderId1, orderId2);

            Order order1 = spy(OrderFixture.createOrderWithItems(1L, 2));
            Order order2 = OrderFixture.createOrderWithItems(2L, 2);
            ReflectionTestUtils.setField(order1, "id", orderId1);
            ReflectionTestUtils.setField(order2, "id", orderId2);
            order2.getItems().forEach(item -> ReflectionTestUtils.setField(item, "status", OrderItemStatus.PAID));

            doThrow(new RuntimeException("예상치 못한 오류")).when(order1).getPayableAmount();
            given(orderRepository.getAllByIdInWithItems(orderIds)).willReturn(List.of(order1, order2));

            // when
            Map<Long, Money> result = orderService.calculateTotalAmounts(orderIds);

            // then - order1은 제외, order2만 포함
            assertThat(result).hasSize(1);
            assertThat(result).containsKey(orderId2);
            assertThat(result).doesNotContainKey(orderId1);
        }
    }

    @Nested
    @DisplayName("주문 만료 처리 (expire)")
    class Expire {

        @Test
        @DisplayName("성공: 주문 ID로 CREATED 상태 주문을 조회하여 만료 처리한다")
        void expire_success() {
            // given
            Long orderId = 1L;
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            ReflectionTestUtils.setField(order, "id", orderId);
            given(orderRepository.getByIdWithItems(orderId)).willReturn(order);

            // when
            orderService.expire(orderId);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
            verify(orderRepository, times(1)).getByIdWithItems(orderId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문 ID이면 ORDER_NOT_FOUND 예외가 발생한다")
        void expire_fail_orderNotFound() {
            // given
            Long orderId = 999L;
            given(orderRepository.getByIdWithItems(orderId))
                    .willThrow(new DomainException(OrderErrorCode.ORDER_NOT_FOUND, "orderId = " + orderId));

            // when & then
            assertThatThrownBy(() -> orderService.expire(orderId))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }
    }
}
