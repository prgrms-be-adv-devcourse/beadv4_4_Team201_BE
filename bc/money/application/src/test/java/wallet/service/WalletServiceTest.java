package wallet.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.event.wallet.WalletChargeCompletedEvent;
import app.giftify.shared.domain.event.wallet.WalletWithdrawnEvent;
import app.giftify.shared.domain.vo.Money;
import domain.errorCode.WalletErrorCode;
import domain.exception.WalletException;
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

        wallet = new Wallet(null, null);
        Field memberId = Wallet.class.getDeclaredField("memberId");
        Field balance = Wallet.class.getDeclaredField("balance");

        id.setAccessible(true);
        memberId.setAccessible(true);
        balance.setAccessible(true);

        id.set(wallet, 1L);
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
        when(walletRepository.findByMemberId(wallet.getMemberId()))
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
        verify(walletRepository).findByMemberId(wallet.getMemberId());
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
        when(walletRepository.findByMemberId(wallet.getMemberId()))
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

        verify(walletRepository).findByMemberId(wallet.getMemberId());
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(eventPublisher, never()).publish(any(WalletChargeCompletedEvent.class));
    }

    @Test
    @DisplayName("지갑 충전 실패 - 저장 과정 중 예외 발생")
    void chargeFailSaveError() {
        // given
        when(walletRepository.findByMemberId(wallet.getMemberId()))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenThrow(new RuntimeException("DB 저장 오류"));

        // when & then
        assertThatThrownBy(() ->
                walletService.charge(
                        wallet.getMemberId(),
                        Money.of(10000L),
                        null,
                        null,
                        null
                ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 오류");

        verify(walletRepository).findByMemberId(wallet.getMemberId());
        verify(walletRepository).save(wallet);
        verify(eventPublisher, never()).publish(any(WalletChargeCompletedEvent.class));
    }

    @Test
    @DisplayName("성공적으로 출금이 수행되면 잔액이 차감되고 이벤트가 발행된다")
    void withdraw_Success() {
        // given
        Long memberId = 1L;
        Money amount = Money.of(10000);
        String transactionType = "WITHDRAW";
        String referenceType = "ORDER";
        Long referenceId = 123L;

        Wallet wallet = Wallet.create(memberId, Money.of(20000)); // 초기 잔액 20,000원
        when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));

        // when
        walletService.withdraw(memberId, amount, transactionType, referenceType, referenceId);

        // then
        // WalletRepository.save() 호출 검증
        verify(walletRepository, times(1)).save(wallet);
        assertThat(wallet.getBalance()).isEqualTo(Money.of(10000)); // 잔액이 10,000원이 남아야 함

        // EventPublisher.publish() 호출 및 이벤트 검증
        ArgumentCaptor<WalletWithdrawnEvent> eventCaptor = ArgumentCaptor.forClass(WalletWithdrawnEvent.class);
        verify(eventPublisher, times(1)).publish(eventCaptor.capture());

        WalletWithdrawnEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getWalletId()).isEqualTo(wallet.getId());
        assertThat(publishedEvent.getAmount()).isEqualTo(amount);
        assertThat(publishedEvent.getBalanceAfter()).isEqualTo(wallet.getBalance());
        assertThat(publishedEvent.getTransactionType()).isEqualTo(transactionType);
        assertThat(publishedEvent.getReferenceType()).isEqualTo(referenceType);
        assertThat(publishedEvent.getReferenceId()).isEqualTo(referenceId);
    }

    @Test
    @DisplayName("지갑이 존재하지 않을 경우 예외가 발생한다")
    void withdraw_Failure_WalletNotFound() {
        // given
        Long memberId = 1L;
        Money amount = Money.of(10000);
        String transactionType = "WITHDRAW";
        String referenceType = "ORDER";
        Long referenceId = 123L;

        when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.withdraw(memberId, amount, transactionType, referenceType, referenceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자가 존재하지 않거나 사용자의 지갑이 존재하지 않습니다.");

        // save 호출되지 않았는지 검증
        verify(walletRepository, never()).save(any(Wallet.class));

        // 이벤트 발행되지 않았는지 검증
        verify(eventPublisher, never()).publish(any(WalletWithdrawnEvent.class));
    }

    @Test
    @DisplayName("출금 금액이 잔액보다 크면 예외가 발생한다")
    void withdraw_Failure_InsufficientFunds() {
        // given
        Long memberId = 1L;
        Money amount = Money.of(30000); // 출금 금액 30,000원
        String transactionType = "WITHDRAW";
        String referenceType = "ORDER";
        Long referenceId = 123L;

        Wallet wallet = Wallet.create(memberId, Money.of(20000)); // 초기 잔액 20,000원
        when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));

        // when & then
        assertThatThrownBy(() -> walletService.withdraw(memberId, amount, transactionType, referenceType, referenceId))
                .isInstanceOf(WalletException.class)
                .hasMessage(WalletErrorCode.INSUFFICIENT_BALANCE.getMessage());

        // save 호출되지 않았는지 검증
        verify(walletRepository, never()).save(wallet);

        // 이벤트 발행되지 않았는지 검증
        verify(eventPublisher, never()).publish(any(WalletWithdrawnEvent.class));
    }

    @Test
    @DisplayName("출금 금액이 null이면 예외가 발생한다")
    void withdraw_Failure_NullAmount() {
        // given
        Long memberId = 1L;
        Money amount = null; // null 금액
        String transactionType = "WITHDRAW";
        String referenceType = "ORDER";
        Long referenceId = 123L;

        Wallet wallet = Wallet.create(memberId, Money.of(20000)); // 초기 잔액 20,000원
        when(walletRepository.findByMemberId(memberId)).thenReturn(Optional.of(wallet));

        // when & then
        assertThatThrownBy(() -> walletService.withdraw(memberId, amount, transactionType, referenceType, referenceId))
                .isInstanceOf(WalletException.class)
                .hasMessage(WalletErrorCode.INVALID_NULL_AMOUNT.getMessage());

        // save 호출되지 않았는지 검증
        verify(walletRepository, never()).save(wallet);

        // 이벤트 발행되지 않았는지 검증
        verify(eventPublisher, never()).publish(any(WalletWithdrawnEvent.class));
    }
}
