package domain.settlement;

import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.OrderItemInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementItemTest {

    private OrderItemInfo createFixtureOrderItemInfo(long amount, long quantity) {
        return new OrderItemInfo(1L, "ORD-001", 100L, quantity, Money.of(amount), LocalDateTime.now());
    }

    @Nested
    @DisplayName("정산 아이템 생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("성공: 결제 대기(PENDING) 상태의 정산 아이템이 생성된다.")
        void create_success() {
            // given
            Long sellerId = 10L;
            OrderItemInfo orderItemInfo = createFixtureOrderItemInfo(20000L, 1L);

            // when
            SettlementItem item = SettlementItem.createPaymentItem(sellerId, orderItemInfo);

            // then
            assertThat(item.getStatus()).isEqualTo(SettlementItemStatus.PENDING);
            assertThat(item.getSellerId()).isEqualTo(sellerId);
            assertThat(item.getTotalAmount()).isEqualTo(Money.of(20000L));
        }

        @Test
        @DisplayName("실패: 판매자 ID가 없으면 생성할 수 없다.")
        void create_fail_null_seller() {
            assertThatThrownBy(() -> SettlementItem.createPaymentItem(null, createFixtureOrderItemInfo(1000L, 1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("판매자 ID는 필수입니다.");
        }

        @Test
        @DisplayName("실패: 정산 대상 금액이 0원 이하이면 생성할 수 없다.")
        void create_fail_invalid_amount() {
            assertThatThrownBy(() -> SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(0L, 1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("정산 대상 금액은 0원보다 커야 합니다.");
        }

        @Test
        @DisplayName("실패: 주문 수량이 1개 미만이면 생성할 수 없다.")
        void create_fail_invalid_quantity() {
            assertThatThrownBy(() -> SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(1000L, 0L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("주문 수량은 1개 이상이어야 합니다.");
        }
    }

    @Nested
    @DisplayName("상태별 데이터 조회 검증 테스트")
    class StateValidationTest {

        @Test
        @DisplayName("실패: PENDING 상태에서 결제 정보 조회 시 예외가 발생한다.")
        void getPaymentInfo_fail_in_pending() {
            // given
            SettlementItem item = SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(10000L, 1L));

            // when & then
            assertThatThrownBy(item::getPaymentInfo)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("결제 대기 상태에서는 결제 정보를 조회할 수 없습니다.");
        }

        @Test
        @DisplayName("실패: PENDING 상태에서 금액 정보(AmountInfo) 조회 시 예외가 발생한다.")
        void getAmountInfo_fail_in_pending() {
            // given
            SettlementItem item = SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(10000L, 1L));

            // when & then
            assertThatThrownBy(item::getAmountInfo)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("결제 대기 상태에서는 금액 정보를 조회할 수 없습니다.");
        }

        @Test
        @DisplayName("실패: COMPLETED가 아닌 상태에서 정산 완료 정보 조회 시 예외가 발생한다.")
        void getSettlementId_fail_not_completed() {
            // given
            SettlementItem item = SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(10000L, 1L));

            // when & then
            assertThatThrownBy(item::getSettlementId)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("정산 완료 상태인 경우에만 정산 정보를 조회할 수 있습니다.");
        }

        @Test
        @DisplayName("실패: CANCELLED가 아닌 상태에서 취소 정보 조회 시 예외가 발생한다.")
        void getAmount_fail_when_cancelled_without_info() {
            assertThatThrownBy(() -> SettlementItem.createPaymentItem(1L, createFixtureOrderItemInfo(1000L, 1L)).getCancelledAt())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("결제 취소인 경우에만 취소 정보를 조회할 수 있습니다.");
        }
    }
}