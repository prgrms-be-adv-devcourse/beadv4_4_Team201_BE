package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaMoneyMember;
import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import domain.member.MoneyMember;
import domain.wallet.Wallet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import vo.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaWalletRepositoryImplTest {

    @Mock
    JpaWalletRepository jpaWalletRepository;

    @InjectMocks
    JpaWalletRepositoryImpl walletRepository;

    @Test
    @DisplayName("지갑 저장 성공")
    void save_wallet() {
        // given
        Wallet wallet = new Wallet(
                1L,
                new MoneyMember(1L),
                Money.zero(),
                null,
                null
        );

        JpaWallet savedJpaWallet = new JpaWallet(
                new JpaMoneyMember(1L)
        );

        when(jpaWalletRepository.save(any(JpaWallet.class)))
                .thenReturn(savedJpaWallet);

        // when
        Wallet result = walletRepository.save(wallet);

        // then
        assertThat(result.getMember().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("지갑 저장 실패 - 이미 존재하는 회원의 지갑 (Unique 제약 조건 위반)")
    void save_wallet_fail_duplicate_member_wallet() {
        // given
        Wallet wallet = new Wallet(
                1L,
                new MoneyMember(1L),
                Money.zero(),
                null,
                null
        );

        when(jpaWalletRepository.save(any(JpaWallet.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for member"));

        // when & then
        assertThatThrownBy(() -> walletRepository.save(wallet))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Duplicate entry");
    }

    @Test
    @DisplayName("지갑 저장 실패 - 존재하지 않는 회원 ID (외래 키 제약 조건 위반)")
    void save_wallet_fail_non_existent_member() {
        // given
        Wallet wallet = new Wallet(
                999L,
                new MoneyMember(999L),
                Money.zero(),
                null,
                null
        );

        when(jpaWalletRepository.save(any(JpaWallet.class)))
                .thenThrow(new DataIntegrityViolationException("Foreign key constraint violation"));

        // when & then
        assertThatThrownBy(() -> walletRepository.save(wallet))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Foreign key");
    }

    @Test
    @DisplayName("지갑 저장 실패 - DB 연결 오류")
    void save_wallet_fail_database_connection_error() {
        // given
        Wallet wallet = new Wallet(
                1L,
                new MoneyMember(1L),
                Money.zero(),
                null,
                null
        );

        when(jpaWalletRepository.save(any(JpaWallet.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // when & then
        assertThatThrownBy(() -> walletRepository.save(wallet))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection");
    }

    @Test
    @DisplayName("지갑 저장 실패 - Null 회원 정보")
    void save_wallet_fail_null_member() {
        // given
        Wallet wallet = new Wallet(
                1L,
                null,
                Money.zero(),
                null,
                null
        );

        when(jpaWalletRepository.save(any(JpaWallet.class)))
                .thenThrow(new IllegalArgumentException("Member cannot be null"));

        // when & then
        assertThatThrownBy(() -> walletRepository.save(wallet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Member cannot be null");
    }
}