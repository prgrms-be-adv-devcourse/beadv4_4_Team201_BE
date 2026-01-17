package domain.wallet;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.shared.domain.vo.Money;

class WalletTest {
    @Test
    @DisplayName("지갑을 생성하면 회원 ID와 초기 잔액이 설정된다")
    void create_wallet_success() {
        // given
        Long memberId = 1L;
        Money balance = Money.of(10_000);

        // when
        Wallet wallet = Wallet.create(memberId, balance);

        // then
        assertThat(wallet.getMemberId()).isEqualTo(memberId);
        assertThat(wallet.getBalance()).isEqualTo(balance);
    }

    @Test
    @DisplayName("지갑에 금액을 충전하면 잔액이 증가한다")
    void charge_success() {
        // given
        Wallet wallet = Wallet.create(1L, Money.of(5_000));
        Money chargeAmount = Money.of(3_000);

        // when
        wallet.charge(chargeAmount);

        // then
        assertThat(wallet.getBalance())
                .isEqualTo(Money.of(8_000L));
    }

    @Test
    @DisplayName("스냅샷으로 지갑을 복원하면 동일한 상태가 된다")
    void restore_wallet_from_snapshot() {
        // given
        WalletSnapshot snapshot = new WalletSnapshot(
                10L,
                1L,
                Money.of(20_000)
        );

        // when
        Wallet wallet = Wallet.restore(snapshot);

        // then
        assertThat(wallet.getMemberId()).isEqualTo(snapshot.memberId());
        assertThat(wallet.getBalance()).isEqualTo(snapshot.balance());
    }

    // Money VO에서 보장
    // 음수, 0원 금액 충전 시 Wallet이 아닌 Money에서 예외를 던짐
    @Test
    @DisplayName("음수 금액을 충전하면 예외가 발생한다")
    void charge_negative_amount_fail() {
    }

    @Test
    @DisplayName("0원 충전 시 예외가 발생한다")
    void charge_zero_amount_fail() {
    }
}
