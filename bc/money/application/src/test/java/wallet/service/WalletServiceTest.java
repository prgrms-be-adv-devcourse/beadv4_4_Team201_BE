package wallet.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.wallet.WalletChargeCompletedEvent;
import app.giftify.shared.domain.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// todo: 지갑 생성 테스트 시나리오 재작성
// todo: 지갑 조회 테스트 시나리오 재작성
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    private static Wallet wallet;

    @BeforeAll
    static void setUpClass() throws Exception {
        Class<? super Wallet> parent = Wallet.class.getSuperclass();
        Field id = parent.getDeclaredField("id");
        Field createdAt = parent.getDeclaredField("createdAt");
        Field updatedAt = parent.getDeclaredField("updatedAt");

        wallet = new Wallet(null, null);
        Field memberId = Wallet.class.getDeclaredField("memberId");
        Field balance = Wallet.class.getDeclaredField("balance");

        id.setAccessible(true);
        createdAt.setAccessible(true);
        updatedAt.setAccessible(true);
        memberId.setAccessible(true);
        balance.setAccessible(true);

        id.set(wallet, 1L);
        createdAt.set(wallet, LocalDateTime.now());
        updatedAt.set(wallet, LocalDateTime.now());
        memberId.set(wallet, 2L);
        balance.set(wallet, Money.of(10000L));
    }

    @Test
    @DisplayName("지갑 생성 성공")
    void createWallet_success() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);

        // when
        Wallet result = walletService.createWallet(wallet.getMemberId());

        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(wallet.getId(), result.getId()),
                () -> assertEquals(wallet.getBalance(), result.getBalance()),
                () -> assertEquals(wallet.getMemberId(), result.getMemberId()),
                () -> assertNotNull(result.getCreatedAt()),
                () -> assertNotNull(result.getUpdatedAt()),
                () -> verify(walletRepository, times(1)).save(any(Wallet.class))
        );
    }

    @Test
    @DisplayName("지갑 생성 실패 - 이미 존재하는 회원의 지갑 (Unique 제약 조건 위반)")
    void createWallet_fail_duplicate_member_wallet() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for member"));

        // when & then
        assertThrows(DataIntegrityViolationException.class,
                () -> walletService.createWallet(wallet.getMemberId()));
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
                () -> walletService.createWallet(wallet.getMemberId()));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    @DisplayName("지갑 생성 실패 - 저장소에서 Null 반환")
    void createWallet_fail_null_returned_from_repository() {
        // given
        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(null);

        // when
        Wallet result = walletService.createWallet(wallet.getMemberId());

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
        when(walletRepository.findById(wallet.getId()))
                .thenReturn(Optional.of(wallet));

        // when
        Wallet result = walletService.getWallet(wallet.getId());

        // then
        assertThat(result.getBalance()).isEqualTo(wallet.getBalance());
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
        when(walletRepository.findById(wallet.getId()))
                .thenThrow(new RuntimeException("데이터베이스 조회 오류"));

        // when & then
        assertThatThrownBy(() -> walletService.getWallet(wallet.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("데이터베이스 조회 오류");
    }

    @Test
    @DisplayName("지갑 충전 성공")
    void chargeSuccess() {
        // given
        when(walletRepository.findByUserId(wallet.getMemberId()))
                .thenReturn(Optional.of(wallet));

        // when
        String transactionType = PaymentType.CHARGE.name();
        String referenceType = Payment.class.getSimpleName();
        Long referenceId = 3L;
        Money amount = Money.of(1000L);
        Money balanceBefore = wallet.getBalance();

        walletService.charge(
                wallet.getMemberId(),
                amount,
                PaymentType.CHARGE.name(),
                Payment.class.getSimpleName(),
                referenceId
        );

        // then
        verify(walletRepository).findByUserId(wallet.getMemberId());
        verify(eventPublisher).publish(any(WalletChargeCompletedEvent.class));

        // 이벤트 내용 검증
        ArgumentCaptor<WalletChargeCompletedEvent> eventCaptor = ArgumentCaptor.forClass(WalletChargeCompletedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        WalletChargeCompletedEvent publishedEvent = eventCaptor.getValue();

        assertAll(
                () -> assertThat(publishedEvent.getWalletId()).isEqualTo(wallet.getId()),
                () -> assertThat(publishedEvent.getAmount()).isEqualTo(amount),
                () -> assertThat(publishedEvent.getTransactionType()).isEqualTo(transactionType),
                () -> assertThat(publishedEvent.getBalanceAfter()).isEqualTo(balanceBefore.plus(amount)),
                () -> assertThat(publishedEvent.getReferenceType()).isEqualTo(referenceType),
                () -> assertThat(publishedEvent.getReferenceId()).isEqualTo(referenceId)
        );
    }

    @Test
    @DisplayName("지갑 충전 실패 - 지갑이 존재하지 않음")
    void chargeFailWalletNotFound() {
        // given
        when(walletRepository.findByUserId(wallet.getMemberId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                walletService.charge(
                        wallet.getMemberId(),
                        null,
                        null,
                        null,
                        null
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자가 존재하지 않거나 사용자의 지갑이 존재하지 않습니다.");

        verify(walletRepository).findByUserId(wallet.getMemberId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(eventPublisher, never()).publish(any(WalletChargeCompletedEvent.class));
    }

    @Test
    @DisplayName("지갑 충전 실패 - 저장 과정 중 예외 발생")
    void chargeFailSaveError() {
        // given
        when(walletRepository.findByUserId(wallet.getMemberId()))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenThrow(new RuntimeException("DB 저장 오류"));

        // when & then
        assertThatThrownBy(() ->
                walletService.charge(
                        wallet.getMemberId(),
                        Money.zero(),
                        null,
                        null,
                        null
                ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 오류");

        verify(walletRepository).findByUserId(wallet.getMemberId());
        verify(walletRepository).save(wallet);
        verify(eventPublisher, never()).publish(any(WalletChargeCompletedEvent.class));
    }
}
