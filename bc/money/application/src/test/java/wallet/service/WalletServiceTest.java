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
import vo.Money;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    @DisplayName("지갑 생성 성공")
    void createWallet_success() {
        // given
        MoneyMember moneyMember = new MoneyMember(1L);
        Long walletId = 1L;

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
}