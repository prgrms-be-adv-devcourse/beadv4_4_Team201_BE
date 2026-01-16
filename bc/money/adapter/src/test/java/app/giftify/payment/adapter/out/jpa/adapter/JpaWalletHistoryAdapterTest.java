package app.giftify.payment.adapter.out.jpa.adapter;

import app.giftify.payment.adapter.out.jpa.entity.JpaWalletHistory;
import app.giftify.payment.adapter.out.jpa.repository.JpaWalletHistoryRepository;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaWalletHistoryAdapterTest {

    @Mock
    private JpaWalletHistoryRepository walletHistoryRepository;

    @InjectMocks
    JpaWalletHistoryAdapter walletHistoryAdapter;

    @Test
    @DisplayName("정상적으로 지갑 히스토리가 저장되는 경우")
    void record_Success() {
        // given
        Long walletId = 10L;
        String transactionType = "PAYMENT";
        Money amount = Money.of(5000);
        Money balanceAfter = Money.of(15000);
        String referenceType = "ORDER";
        Long referenceId = 1234L;

        // when
        walletHistoryAdapter.record(
                walletId,
                transactionType,
                amount,
                balanceAfter,
                referenceType,
                referenceId
        );

        // then
        // JpaWalletHistoryRepository.save()가 호출됐는지 확인
        ArgumentCaptor<JpaWalletHistory> captor = ArgumentCaptor.forClass(JpaWalletHistory.class);
        verify(walletHistoryRepository, times(1)).save(captor.capture());

        // 저장된 엔티티 검증
        JpaWalletHistory capturedWalletHistory = captor.getValue();
        assertThat(capturedWalletHistory.getWalletId()).isEqualTo(walletId);
        assertThat(capturedWalletHistory.getTransactionType()).isEqualTo(transactionType);
        assertThat(capturedWalletHistory.getAmount().amount()).isEqualTo(new BigDecimal("5000"));
        assertThat(capturedWalletHistory.getBalanceAfter().amount()).isEqualTo(new BigDecimal("15000"));
        assertThat(capturedWalletHistory.getReferenceType()).isEqualTo(referenceType);
        assertThat(capturedWalletHistory.getReferenceId()).isEqualTo(referenceId);
    }

    @Test
    @DisplayName("저장 중 예외가 발생하는 경우 예외가 전파되는지 확인")
    void record_Failure() {
        // given
        Long walletId = 10L;
        String transactionType = "PAYMENT";
        Money amount = Money.of(5000);
        Money balanceAfter = Money.of(15000);
        String referenceType = "ORDER";
        Long referenceId = 1234L;

        // JpaWalletHistoryRepository.save()가 예외를 발생시키도록 설정
        doThrow(new RuntimeException("데이터베이스 오류"))
                .when(walletHistoryRepository).save(any(JpaWalletHistory.class));

        // when & then
        try {
            walletHistoryAdapter.record(walletId, transactionType, amount, balanceAfter, referenceType, referenceId);
        } catch (RuntimeException e) {
            // 예외 메시지 확인
            assertThat(e.getMessage()).isEqualTo("데이터베이스 오류");
        }

        // save 호출 검증
        verify(walletHistoryRepository, times(1)).save(any(JpaWalletHistory.class));
    }
}