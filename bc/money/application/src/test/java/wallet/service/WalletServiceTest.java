package wallet.service;

import domain.member.MoneyMember;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import vo.Money;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private final long memberId = 1L;
    private final long walletId = 2L;

    @Test
    @DisplayName("지갑 생성 성공")
    void createWallet_success() {
        // given
        MoneyMember moneyMember = new MoneyMember(memberId);

        Wallet savedWallet = new Wallet(
                walletId,
                moneyMember,
                Money.zero(),
                null,
                null
        );

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(savedWallet);

        // when
        Wallet result = walletService.createWallet(moneyMember);

        // then
        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(moneyMember.getId(), result.getMember().getId());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 이미 존재하는 회원의 지갑 (Unique 제약 조건 위반)")
    void createWallet_fail_duplicate_member_wallet() {
        // given
        MoneyMember moneyMember = new MoneyMember(memberId);

        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for member"));

        // when & then
        assertThrows(DataIntegrityViolationException.class,
                () -> walletService.createWallet(moneyMember));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 존재하지 않는 회원 ID (외래 키 제약 조건 위반)")
    void createWallet_fail_non_existent_member() {
        // given
        MoneyMember nonExistentMember = new MoneyMember(999L);

        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Foreign key constraint violation"));

        // when & then
        assertThrows(DataIntegrityViolationException.class,
                () -> walletService.createWallet(nonExistentMember));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - Null 회원 정보")
    void createWallet_fail_null_member() {
        // given
        MoneyMember nullMember = null;

        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new IllegalArgumentException("Member cannot be null"));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(nullMember));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 저장소 오류")
    void createWallet_fail_repository_error() {
        // given
        MoneyMember moneyMember = new MoneyMember(memberId);

        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new RuntimeException("Repository access error"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> walletService.createWallet(moneyMember));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 저장소에서 Null 반환")
    void createWallet_fail_null_returned_from_repository() {
        // given
        MoneyMember moneyMember = new MoneyMember(memberId);

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(null);

        // when
        Wallet result = walletService.createWallet(moneyMember);

        // then
        assertNull(result);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 회원 ID가 0 또는 음수")
    void createWallet_fail_invalid_member_id() {
        // given
        MoneyMember invalidMember = new MoneyMember(0L);

        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new IllegalArgumentException("Invalid member ID"));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(invalidMember));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 잔액 조회 성공")
    void getBalance_success() {
        // given
        Money expectedBalance = Money.of(10000);

        Wallet wallet = new Wallet(
                walletId,
                new MoneyMember(memberId),
                expectedBalance,
                null,
                null
        );

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        // when
        Money result = walletService.getBalance(walletId);

        // then
        assertThat(result).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("지갑 잔액 조회 실패 - 존재하지 않는 지갑 ID")
    void getBalance_fail_wallet_not_found() {
        // given
        Long notExistWalletId = 999L;

        when(walletRepository.findById(notExistWalletId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.getBalance(notExistWalletId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않은 지갑입니다.");
    }

    @Test
    @DisplayName("지갑 잔액 조회 실패 - Null 지갑 ID")
    void getBalance_fail_null_wallet_id() {
        // given
        Long nullWalletId = null;

        when(walletRepository.findById(nullWalletId))
                .thenThrow(new IllegalArgumentException("지갑 ID는 null일 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> walletService.getBalance(nullWalletId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지갑 ID는 null일 수 없습니다.");
    }

    @Test
    @DisplayName("지갑 잔액 조회 실패 - 음수 지갑 ID")
    void getBalance_fail_negative_wallet_id() {
        // given
        Long negativeWalletId = -1L;

        when(walletRepository.findById(negativeWalletId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.getBalance(negativeWalletId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않은 지갑입니다.");
    }

    @Test
    @DisplayName("지갑 잔액 조회 실패 - Repository 예외 발생")
    void getBalance_fail_repository_exception() {
        // given
        when(walletRepository.findById(walletId))
                .thenThrow(new RuntimeException("데이터베이스 조회 오류"));

        // when & then
        assertThatThrownBy(() -> walletService.getBalance(walletId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("데이터베이스 조회 오류");
    }
}