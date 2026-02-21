package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderTest {

    @Test
    @DisplayName("성공: 주문 생성 상태에서 결제 완료로 정상 전이된다")
    void paid_success() {
        // given
        Order order = OrderFixture.createOrderWithStatus(OrderStatus.CREATED);
        Long paymentId = 1L;
        String lastTransactionKey = "TX_KEY_456";
        LocalDateTime paidAt = LocalDateTime.now();

        // when
        order.paid(paymentId, lastTransactionKey);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        assertThat(order.getOriginTransactionKey()).isEqualTo(lastTransactionKey);
        assertNotNull(order.getPaidAt());
    }

    @Test
    @DisplayName("성공: 주문이 결제 완료되면 모든 주문 아이템도 완료 상태가 된다")
    void paid_items_propagation() {
        // given
        Order order = OrderFixture.createOrderWithItems(1L, 2); // 아이템 2개 포함

        // when
        order.paid(1L, "tx");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getItems()).allMatch(item -> item.getStatus() == OrderItemStatus.PAID);
    }

    @Test
    @DisplayName("실패: 주문 생성 상태 외에 결제 완료 처리하면 예외가 발생한다")
    void paid_fail_invalid_status() {
        // given
        Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);

        // when & then
        assertThatThrownBy(() ->
                order.paid(1L, "tx")
        )
                .isInstanceOf(PolicyException.class)
                .hasMessageContaining(String.format("주문 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", order.getStatus()))
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
    }

    @Nested
    @DisplayName("주문 생성 (create)")
    class Create {

        @Test
        @DisplayName("성공: 유효한 정보로 주문을 생성하면 CREATED 상태로 초기화된다")
        void create_success() {
            // given
            List<OrderItem> items = List.of(
                    OrderItem.create(1L, TargetType.FUNDING, OrderItemType.FUNDING_GIFT, 1L, 2L,
                            Money.of("10000"), Money.of("5000"))
            );

            // when
            Order order = Order.create(1L, items, PaymentMethod.DEPOSIT);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(order.getBuyerId()).isEqualTo(1L);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.DEPOSIT);
            assertThat(order.getTotalAmount()).isEqualTo(Money.of("5000"));
            assertNotNull(order.getOrderNumber());
        }

        @Test
        @DisplayName("실패: buyerId가 null이면 INVALID_BUYER_ID 예외가 발생한다")
        void create_fail_nullBuyerId() {
            // given
            List<OrderItem> items = List.of(
                    OrderItem.create(1L, TargetType.FUNDING, OrderItemType.FUNDING_GIFT, 1L, 2L,
                            Money.of("10000"), Money.of("5000"))
            );

            // when & then
            assertThatThrownBy(() -> Order.create(null, items, PaymentMethod.DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_BUYER_ID);
        }

        @Test
        @DisplayName("실패: items가 비어있으면 INVALID_ORDER_ITEM 예외가 발생한다")
        void create_fail_emptyItems() {
            // when & then
            assertThatThrownBy(() -> Order.create(1L, List.of(), PaymentMethod.DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_ORDER_ITEM);
        }

        @Test
        @DisplayName("실패: paymentMethod가 null이면 INVALID_PAYMENT_METHOD 예외가 발생한다")
        void create_fail_nullPaymentMethod() {
            // given
            List<OrderItem> items = List.of(
                    OrderItem.create(1L, TargetType.FUNDING, OrderItemType.FUNDING_GIFT, 1L, 2L,
                            Money.of("10000"), Money.of("5000"))
            );

            // when & then
            assertThatThrownBy(() -> Order.create(1L, items, null))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_PAYMENT_METHOD);
        }

        @Test
        @DisplayName("실패: 총 금액이 1000원 미만이면 INVALID_TOTAL_AMOUNT 예외가 발생한다")
        void create_fail_invalidTotalAmount() {
            // given - amount 500원 아이템 1개 → 총합 500원 < 1000원
            List<OrderItem> items = List.of(
                    OrderItem.create(1L, TargetType.FUNDING, OrderItemType.FUNDING_GIFT, 1L, 2L,
                            Money.of("10000"), Money.of("500"))
            );

            // when & then
            assertThatThrownBy(() -> Order.create(1L, items, PaymentMethod.DEPOSIT))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_TOTAL_AMOUNT);
        }
    }

    @Nested
    @DisplayName("전체 취소 (cancelAll)")
    class CancelAll {

        @Test
        @DisplayName("성공: CREATED 상태 주문 취소 시 모든 아이템과 주문이 CANCELED 상태가 된다")
        void cancelAll_success_created() {
            // given
            Order order = OrderFixture.createOrderWithItems(1L, 2);

            // when
            order.cancelAll();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            assertThat(order.getItems()).allMatch(item -> item.getStatus() == OrderItemStatus.CANCELED);
        }

        @Test
        @DisplayName("성공: PAID 상태 주문 취소 시 모든 아이템이 CANCELING, 주문이 CANCELING 상태가 된다")
        void cancelAll_success_paid() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.PAID);

            // when
            order.cancelAll();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELING);
            assertThat(order.getItems()).allMatch(item -> item.getStatus() == OrderItemStatus.CANCELING);
        }

        @Test
        @DisplayName("실패: CONFIRMED 상태 주문은 취소 불가 - INVALID_STATUS_TRANSITION 예외가 발생한다")
        void cancelAll_fail_confirmed() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CONFIRMED);

            // when & then
            assertThatThrownBy(order::cancelAll)
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        @Test
        @DisplayName("실패: 이미 취소된 주문은 재취소 불가 - INVALID_STATUS_TRANSITION 예외가 발생한다")
        void cancelAll_fail_alreadyCanceled() {
            // given
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);

            // when & then
            assertThatThrownBy(order::cancelAll)
                    .isInstanceOf(PolicyException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    @Nested
    @DisplayName("상태 동기화 (synchronizeStatus)")
    class SynchronizeStatus {

        @Test
        @DisplayName("모든 아이템이 CANCELED이면 주문 상태가 CANCELED로 동기화된다")
        void synchronizeStatus_allCanceled() {
            // given - 아이템을 CANCELING → CANCELED 로 전환
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);
            order.getItems().forEach(item ->
                    ReflectionTestUtils.setField(item, "status", OrderItemStatus.CANCELED));

            // when
            order.synchronizeStatus();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @Test
        @DisplayName("모든 아이템이 CANCELING이면 주문 상태가 CANCELING으로 동기화된다")
        void synchronizeStatus_allCanceling() {
            // given - fixture가 CANCELING 상태일 때 아이템도 CANCELING으로 설정
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);

            // when
            order.synchronizeStatus();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELING);
        }

        @Test
        @DisplayName("일부 아이템만 CANCELING이면 주문 상태가 PARTIAL_CANCELING으로 동기화된다")
        void synchronizeStatus_partialCanceling() {
            // given - PAID 주문의 아이템 중 하나만 CANCELING
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            order.paid(1L, "tx");
            ReflectionTestUtils.setField(order.getItems().get(0), "status", OrderItemStatus.CANCELING);

            // when
            order.synchronizeStatus();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIAL_CANCELING);
        }

        @Test
        @DisplayName("일부 아이템이 CANCELED이고 나머지가 PAID이면 주문 상태가 PARTIAL_CANCELED로 동기화된다")
        void synchronizeStatus_partialCanceled() {
            // given - PAID 주문의 아이템 중 하나만 CANCELED
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            order.paid(1L, "tx");
            ReflectionTestUtils.setField(order.getItems().get(0), "status", OrderItemStatus.CANCELED);

            // when
            order.synchronizeStatus();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PARTIAL_CANCELED);
        }
    }

    @Nested
    @DisplayName("취소 중인 아이템 조회 (getCancelingItems)")
    class GetCancelingItems {

        @Test
        @DisplayName("CANCELING 상태 아이템만 반환한다")
        void getCancelingItems_returnsOnlyCancelingItems() {
            // given - fixture가 CANCELING 상태일 때 모든 아이템이 CANCELING
            Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELING);

            // when
            List<OrderItem> result = order.getCancelingItems();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(item -> item.getStatus() == OrderItemStatus.CANCELING);
        }

        @Test
        @DisplayName("CANCELING 상태 아이템이 없으면 빈 리스트를 반환한다")
        void getCancelingItems_returnsEmptyWhenNone() {
            // given - 모든 아이템이 CREATED 상태
            Order order = OrderFixture.createOrderWithItems(1L, 2);

            // when
            List<OrderItem> result = order.getCancelingItems();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("부분 취소 유효성 검증 (validatePartialCancelable)")
    class ValidatePartialCancelable {

        @Test
        @DisplayName("성공: 취소 가능한 아이템 ID를 모두 요청하면 해당 아이템 목록을 반환한다")
        void validatePartialCancelable_success() {
            // given
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            ReflectionTestUtils.setField(order.getItems().get(0), "id", 1L);
            ReflectionTestUtils.setField(order.getItems().get(1), "id", 2L);

            // when
            List<OrderItem> result = order.validatePartialCancelable(List.of(1L, 2L));

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("실패: 주문에 존재하지 않는 아이템 ID만 요청하면 ORDER_ITEM_NOT_FOUND 예외가 발생한다")
        void validatePartialCancelable_fail_notFound() {
            // given
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            ReflectionTestUtils.setField(order.getItems().get(0), "id", 1L);
            ReflectionTestUtils.setField(order.getItems().get(1), "id", 2L);

            // when & then
            assertThatThrownBy(() -> order.validatePartialCancelable(List.of(999L)))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_ORDER_ITEM);
        }

        @Test
        @DisplayName("실패: 요청한 아이템 중 일부가 주문에 없으면 INVALID_ORDER_ITEM 예외가 발생한다")
        void validatePartialCancelable_fail_partialMismatch() {
            // given
            Order order = OrderFixture.createOrderWithItems(1L, 2);
            ReflectionTestUtils.setField(order.getItems().get(0), "id", 1L);
            ReflectionTestUtils.setField(order.getItems().get(1), "id", 2L);

            // when & then - 존재하는 1L과 존재하지 않는 999L 함께 요청
            assertThatThrownBy(() -> order.validatePartialCancelable(List.of(1L, 999L)))
                    .isInstanceOf(DomainException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.INVALID_ORDER_ITEM);
        }
    }
}