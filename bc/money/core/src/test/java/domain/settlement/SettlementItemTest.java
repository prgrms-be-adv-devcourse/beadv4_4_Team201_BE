package domain.settlement;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.FeeInfo;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.OrderItemInfo;
import app.giftify.shared.domain.vo.PaymentInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class SettlementItemTest {

    private final Long sellerId = 100L;

    @Nested
    @DisplayName("신규 주문 생성 테스트 (create 메서드)")
    class CreateTest {

        @Test
        @DisplayName("유효한 정보로 생성 시 PENDING 상태의 아이템이 생성된다")
        void create_success() {
            // given
            OrderItemInfo itemInfo = createOrderItemInfo(10000L, 1L);

            // when
            SettlementItem item = SettlementItem.create(sellerId, itemInfo);

            // then
            assertAll(
                    () -> assertThat(item.getStatus()).isEqualTo(SettlementItemStatus.PENDING),
                    () -> assertThat(item.getSellerId()).isEqualTo(sellerId),
                    () -> assertThat(item.getSettlementAmount()).isEqualTo(Money.zero()), // 결제 전이므로 0원
                    () -> assertThat(item.getPaymentInfo()).isNull(),
                    () -> assertThat(item.getSettlementId()).isNull()
            );
        }

        @Test
        @DisplayName("정산 금액이 0원 이하인 주문은 생성이 실패한다")
        void create_fail_invalid_amount() {
            OrderItemInfo zeroAmountItem = createOrderItemInfo(0L, 1L);

            assertThatThrownBy(() -> SettlementItem.create(sellerId, zeroAmountItem))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0원보다 커야 합니다");
        }

        @Test
        @DisplayName("주문 수량이 1개 미만인 주문은 생성이 실패한다")
        void create_fail_invalid_quantity() {
            OrderItemInfo invalidQuantityItem = createOrderItemInfo(10000L, 0L);

            assertThatThrownBy(() -> SettlementItem.create(sellerId, invalidQuantityItem))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("수량은 1개 이상");
        }
    }

    @Nested
    @DisplayName("생성자 상태 정합성 검증 테스트")
    class ConstructorInvariantsTest {

        @Test
        @DisplayName("결제 대기(PENDING) 상태인데 결제 정보가 포함되면 예외가 발생한다")
        void pending_status_with_payment_info_fails() {
            // given
            OrderItemInfo itemInfo = createOrderItemInfo(10000L, 1L);
            PaymentInfo mockPayment = new PaymentInfo("key", "t-key", PaymentMethodType.WALLET);

            // then
            assertThatThrownBy(() ->
                    invokePrivateConstructor(SettlementItemStatus.PENDING, null, mockPayment, null)
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("결제 및 수수료 정보가 존재할 수 없습니다");
        }

        @Test
        @DisplayName("결제 대기(PENDING) 상태인데 settlementId가 존재하면 예외가 발생한다")
        void pending_status_with_settlement_id_fails() {
            // given
            OrderItemInfo itemInfo = createOrderItemInfo(10000L, 1L);

            // then
            assertThatThrownBy(() ->
                    invokePrivateConstructor(SettlementItemStatus.PENDING, 500L, null, null)
            ).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("정산 그룹 ID가 존재할 수 없습니다");
        }
    }

    // --- Helper Methods ---

    private OrderItemInfo createOrderItemInfo(long amount, long quantity) {
        return new OrderItemInfo(
                1L,
                "ORD-2026-001",
                100L,
                quantity,
                Money.of(amount),
                LocalDateTime.now()
        );
    }

    /**
     * 테스트를 위해 private 생성자 호출 로직을 모사 (실제 도메인 클래스의 생성자 파라미터와 일치)
     */
    private SettlementItem invokePrivateConstructor(
            SettlementItemStatus status,
            Long settlementId,
            PaymentInfo paymentInfo,
            FeeInfo feeInfo
    ) {
        // Reflection을 쓰거나 패키지 내 접근을 활용할 수 있으나,
        // 여기서는 이해를 돕기 위해 직접 작성을 가정한 호출입니다.
        try {
            var constructor = SettlementItem.class.getDeclaredConstructor(
                    Long.class, Long.class, Long.class, OrderItemInfo.class,
                    PaymentInfo.class, FeeInfo.class, Long.class,
                    SettlementItemType.class, SettlementItemStatus.class, java.time.LocalDate.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    null, 100L, settlementId, createOrderItemInfo(10000L, 1L),
                    paymentInfo, feeInfo, null,
                    SettlementItemType.ITEM_PAYMENT, status, null
            );
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }
}