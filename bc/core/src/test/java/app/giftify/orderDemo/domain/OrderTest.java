package app.giftify.orderDemo.domain;

import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.fixture.OrderFixture;
import app.giftify.shared.api.exception.PolicyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderTest {

    @Test
    @DisplayName("성공: 주문 생성 상태에서 결제 완료로 정상 전이된다")
    void toPaid_success() {
        // given
        Order order = OrderFixture.createOrderWithStatus(OrderStatus.CREATED);
        String paymentKey = "PG_KEY_123";
        String lastTransactionKey = "TX_KEY_456";
        LocalDateTime paidAt = LocalDateTime.now();

        // when
        order.toPaid(paymentKey, lastTransactionKey);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(order.getOriginTransactionKey()).isEqualTo(lastTransactionKey);
        assertNotNull(order.getPaidAt());
    }

    @Test
    @DisplayName("성공: 주문이 결제 완료되면 모든 주문 아이템도 완료 상태가 된다")
    void toPaid_items_propagation() {
        // given
        Order order = OrderFixture.createOrderWithItems(1L, 2); // 아이템 2개 포함

        // when
        order.toPaid("key", "tx");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getItems()).allMatch(item -> item.getStatus() == OrderItemStatus.PAID);
    }

    @Test
    @DisplayName("실패: 주문 생성 상태 외에 결제 완료 처리하면 예외가 발생한다")
    void toPaid_fail_invalid_status() {
        // given
        Order order = OrderFixture.createOrderWithStatus(OrderStatus.CANCELED);

        // when & then
        assertThatThrownBy(() ->
                order.toPaid("key", "tx")
        )
                .isInstanceOf(PolicyException.class)
                .hasMessageContaining(String.format("주문 결제 완료는 생성 상태에서만 가능합니다. (현재: %s)", order.getStatus()))
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_STATUS_TRANSITION);
    }
}