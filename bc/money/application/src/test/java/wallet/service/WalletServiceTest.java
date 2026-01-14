package wallet.service;

import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import domain.wallet.WalletSnapshot;
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
        WalletSnapshot snapshot = new WalletSnapshot(
                walletId,
                memberId,
                Money.zero(),
                null,
                null
        );
        Wallet savedWallet = Wallet.restore(snapshot);

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(savedWallet);

        // when
        Wallet result = walletService.createWallet(memberId);

        // then
        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals(memberId, result.getMemberId());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 이미 존재하는 회원의 지갑 (Unique 제약 조건 위반)")
    void createWallet_fail_duplicate_member_wallet() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for member"));

        // when & then
        assertThrows(DataIntegrityViolationException.class,
                () -> walletService.createWallet(memberId));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 존재하지 않는 회원 ID (외래 키 제약 조건 위반)")
    void createWallet_fail_non_existent_member() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Foreign key constraint violation"));

        // when & then
        assertThrows(DataIntegrityViolationException.class,
                () -> walletService.createWallet(999L));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - Null 회원 정보")
    void createWallet_fail_null_member() {
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new IllegalArgumentException("Member cannot be null"));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(null));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 저장소 오류")
    void createWallet_fail_repository_error() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new RuntimeException("Repository access error"));

        // when & then
        assertThrows(RuntimeException.class,
                () -> walletService.createWallet(memberId));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 저장소에서 Null 반환")
    void createWallet_fail_null_returned_from_repository() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(null);

        // when
        Wallet result = walletService.createWallet(memberId);

        // then
        assertNull(result);
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 회원 ID가 0 또는 음수")
    void createWallet_fail_invalid_member_id() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new IllegalArgumentException("Invalid member ID"));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(0L));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 조회 성공")
    void getBalance_success() {
        // given
        Money expectedBalance = Money.of(10000);

        WalletSnapshot snapshot = new WalletSnapshot(
                walletId,
                memberId,
                expectedBalance,
                null,
                null
        );
        Wallet wallet = Wallet.restore(snapshot);

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        // when
        Wallet result = walletService.getWallet(walletId);

        // then
        assertThat(result.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("지갑 조회 실패 - 존재하지 않는 지갑 ID")
    void getBalance_fail_wallet_not_found() {
        // given
        when(walletRepository.findById(999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.getWallet(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않은 지갑입니다.");
    }

    @Test
    @DisplayName("지갑 조회 실패 - Null 지갑 ID")
    void getBalance_fail_null_wallet_id() {
        // given
        when(walletRepository.findById(null))
                .thenThrow(new IllegalArgumentException("지갑 ID는 null일 수 없습니다."));

        // when & then
        assertThatThrownBy(() -> walletService.getWallet(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지갑 ID는 null일 수 없습니다.");
    }

    @Test
    @DisplayName("지갑 조회 실패 - 음수 지갑 ID")
    void getBalance_fail_negative_wallet_id() {
        // given
        when(walletRepository.findById(-1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.getWallet(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않은 지갑입니다.");
    }

    @Test
    @DisplayName("지갑 조회 실패 - Repository 예외 발생")
    void getBalance_fail_repository_exception() {
        // given
        when(walletRepository.findById(walletId))
                .thenThrow(new RuntimeException("데이터베이스 조회 오류"));

        // when & then
        assertThatThrownBy(() -> walletService.getWallet(walletId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("데이터베이스 조회 오류");
    }
}