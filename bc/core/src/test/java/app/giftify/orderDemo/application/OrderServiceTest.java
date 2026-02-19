package app.giftify.orderDemo.application;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.orderDemo.adapter.outbound.client.WishlistClient;
import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.OrderStatus;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
    private WishlistClient wishlistClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private final Long buyerId = 1L;
    private final Long wishlistItemId = 100L;
    private final Long productId = 500L;

    private final PlaceOrderItemRequest itemRequest = new PlaceOrderItemRequest(
            wishlistItemId,
            2002L,
            Money.of("15000"),
            OrderItemType.FUNDING_GIFT
    );

    private final CreateOrderCommand command = new CreateOrderCommand(
            buyerId,
            PaymentMethod.DEPOSIT,
            List.of(itemRequest)
    );

    private final Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap = Map.of(
            wishlistItemId,
            new WishlistItemSnapshot(
                    wishlistItemId,
                    productId,
                    "productName",
                    200000,
                    200L,
                    2002L
            )
    );

    private final List<Long> wishlistItemIds = List.of(wishlistItemId);

    @Test
    @DisplayName("일반 선물 주문 생성 성공 - 모든 단계가 정상적으로 수행된다")
    void createOrder_success_normalGift() {
        // given
        given(wishlistClient.getSnapshotList(wishlistItemIds)).willReturn(wishlistItemSnapshotMap);

        // 저장 로직 모킹 (ID와 Number가 포함된 Order 반환)
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        // when
        OrderSnapshot result = orderService.createOrder(command, List.of());

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderItemSnapshots()).hasSize(1);

        // 3. 검증: 스냅샷 조회, 저장, 이벤트 발행 호출 여부
        verify(wishlistClient, times(1)).getSnapshotList(wishlistItemIds);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    @DisplayName("실패 - 위시리스트 스냅샷 정보를 찾을 수 없는 경우 예외가 발생한다")
    void createOrder_fail_snapshotNotFound() {
        // given
        // 존재하지 않는 맵 상황 시뮬레이션 (빈 리스트 반환)
        given(wishlistClient.getSnapshotList(wishlistItemIds)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(command, List.of()))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.SNAPSHOTS_NOT_FOUND);
    }

    @Test
    @DisplayName("펀딩 참여 주문 - fundingId가 OrderItem에 정상적으로 매핑된다")
    void createOrder_success_withFundingId() {
        // given
        Long fundingId = 500L;

        FundingSnapshot fundingSnapshot = new FundingSnapshot(fundingId, wishlistItemId);

        given(wishlistClient.getSnapshotList(wishlistItemIds)).willReturn(wishlistItemSnapshotMap);
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        OrderSnapshot result = orderService.createOrder(command, List.of(fundingSnapshot));

        // then
        // OrderItem의 targetId가 fundingId와 일치하는지 확인
        assertThat(result.orderItemSnapshots().get(0).targetId()).isEqualTo(fundingId);
        assertThat(result.orderItemSnapshots().get(0).targetType()).isEqualTo(TargetType.FUNDING);
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
    @DisplayName("결제 완료 처리 (markOrderAsPaid)")
    class MarkOrderAsPaid {

        @Test
        @DisplayName("성공: 주문을 조회하여 결제 완료 상태로 변경한다")
        void success() {
            // given
            Order order = spy(OrderFixture.createOrderWithItems(1L, 2)); // 엔티티 메서드 호출 확인을 위해 spy 사용

            String orderNumber = "orderNumber";

            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(
                    "orderNumber",
                    "PG_KEY_123",
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
            verify(order).toPaid(command.paymentKey(), command.lastTransactionKey());

            // 3. 실제 상태가 변했는지 확인 (더티 체킹에 의해 반영될 상태)
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 주문번호일 경우 예외가 발생한다")
        void fail_not_found() {
            // given
            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(
                    "orderNumber",
                    "PG_KEY_123",
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
                    "PG_KEY_123",
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
}