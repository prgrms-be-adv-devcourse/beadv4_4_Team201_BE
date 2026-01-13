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
import vo.Money;

import static org.assertj.core.api.Assertions.assertThat;
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
}