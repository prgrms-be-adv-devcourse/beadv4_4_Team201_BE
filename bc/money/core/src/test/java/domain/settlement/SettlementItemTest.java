package domain.settlement;

import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementItemTest {

    @Test
    @DisplayName("PAYMENT 타입의 정산 아이템을 정상적으로 생성한다")
    void createSettlementItem_payment() {
        // given
        String orderId = "Qwe_123";
        String paymentKey = "payment-key-123";
        Long sellerId = 10L;

        Money totalAmount = Money.of(10_000);
        Money platformFee = Money.of(1_000);
        Money pgFee = Money.of(300);
        Money settlementAmount = Money.of(8_700);

        LocalDateTime settlementDate = LocalDateTime.of(2026, 1, 31, 0, 0);

        // when
        SettlementItem item = SettlementItem.builder()
                .id(100L)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .sellerId(sellerId)
                .type(SettlementType.PAYMENT)
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .pgFee(pgFee)
                .settlementAmount(settlementAmount)
                .status(SettlementStatus.READY)
                .settlementDate(settlementDate)
                .build();

        // then
        assertThat(item.getId()).isEqualTo(100L);
        assertThat(item.getOrderId()).isEqualTo(orderId);
        assertThat(item.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(item.getSellerId()).isEqualTo(sellerId);
        assertThat(item.getType()).isEqualTo(SettlementType.PAYMENT);
        assertThat(item.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(item.getPlatformFee()).isEqualTo(platformFee);
        assertThat(item.getPgFee()).isEqualTo(pgFee);
        assertThat(item.getSettlementAmount()).isEqualTo(settlementAmount);
        assertThat(item.getStatus()).isEqualTo(SettlementStatus.READY);
        assertThat(item.getSettlementDate()).isEqualTo(settlementDate);
    }

    @Test
    @DisplayName("CANCEL 타입의 정산 아이템을 생성할 수 있다")
    void createSettlementItem_cancel() {
        // given
        Money totalAmount = Money.of(5_000);

        // when
        SettlementItem item = SettlementItem.builder()
                .orderId("Qwe_123")
                .paymentKey("cancel-key")
                .sellerId(20L)
                .type(SettlementType.CANCEL)
                .totalAmount(totalAmount)
                .platformFee(Money.zero())
                .pgFee(Money.zero())
                .settlementAmount(totalAmount)
                .status(SettlementStatus.READY)
                .settlementDate(LocalDateTime.now())
                .build();

        // then
        assertThat(item.getType()).isEqualTo(SettlementType.CANCEL);
        assertThat(item.getTotalAmount()).isEqualTo(totalAmount);
    }

    @Test
    @DisplayName("ID 없이 생성하면 신규 도메인 객체로 간주한다")
    void createSettlementItem_withoutId() {
        // when
        SettlementItem item = SettlementItem.builder()
                .orderId("Qwe_123")
                .paymentKey("no-id-key")
                .sellerId(30L)
                .type(SettlementType.PAYMENT)
                .totalAmount(Money.of(1_000))
                .platformFee(Money.of(100))
                .pgFee(Money.of(50))
                .settlementAmount(Money.of(850))
                .status(SettlementStatus.READY)
                .settlementDate(LocalDateTime.now())
                .build();

        // then
        assertThat(item.getId()).isNull();
    }
}