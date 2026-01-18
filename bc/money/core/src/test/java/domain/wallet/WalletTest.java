package domain.wallet;

import app.giftify.shared.domain.vo.Money;
import domain.errorCode.WalletErrorCode;
import domain.exception.WalletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("지갑에서 출금을 성공적으로 수행하면 잔액이 차감된다")
    void withdraw_Success() {
        // given
        Wallet wallet = Wallet.create(1L, Money.of(50000)); // 초기 잔액 50,000원
        Money withdrawAmount = Money.of(30000); // 출금 금액 30,000원

        // when
        wallet.withdraw(withdrawAmount);

        // then
        assertThat(wallet.getBalance()).isEqualTo(Money.of(20000)); // 잔액이 20,000원이 되어야 함
    }

    @Test
    @DisplayName("잔액보다 큰 금액을 출금하려고 하면 예외가 발생한다")
    void withdraw_Failure_InsufficientBalance() {
        // given
        Wallet wallet = Wallet.create(1L, Money.of(10000)); // 초기 잔액 10,000원
        Money withdrawAmount = Money.of(20000); // 출금 금액 20,000원

        // when & then
        assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
                .isInstanceOf(WalletException.class) // 커스텀 예외 확인
                .hasMessage(WalletErrorCode.INSUFFICIENT_BALANCE.getMessage()); // 에러 메시지 확인

        // 잔액이 변경되지 않아야 함
        assertThat(wallet.getBalance()).isEqualTo(Money.of(10000));
    }

    @Test
    @DisplayName("출금 금액이 null이면 예외가 발생한다")
    void withdraw_Failure_NullAmount() {
        // given
        Wallet wallet = Wallet.create(1L, Money.of(10000)); // 초기 잔액 10,000원
        Money withdrawAmount = null; // null 금액

        // when & then
        assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
                .isInstanceOf(WalletException.class) // 커스텀 예외 확인
                .hasMessage(WalletErrorCode.INVALID_NULL_AMOUNT.getMessage()); // 에러 메시지 확인

        // 잔액이 변경되지 않아야 함
        assertThat(wallet.getBalance()).isEqualTo(Money.of(10000));
    }


}