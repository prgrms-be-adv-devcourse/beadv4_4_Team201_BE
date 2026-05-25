package app.giftify.wallet.adapter.inbound.web;

import app.giftify.support.common.money.Money;
import app.giftify.wallet.application.inbound.QueryWalletHistoryUseCase;
import app.giftify.wallet.application.inbound.QueryWalletUseCase;
import app.giftify.wallet.application.inbound.WalletBalanceResult;
import app.giftify.wallet.application.inbound.WalletHistoryQuery;
import app.giftify.wallet.application.inbound.WalletHistoryResult;
import app.giftify.wallet.application.inbound.WithdrawStatus;
import app.giftify.wallet.application.inbound.WithdrawWalletCommand;
import app.giftify.wallet.application.inbound.WithdrawWalletResult;
import app.giftify.wallet.application.inbound.WithdrawWalletUseCase;
import app.giftify.wallet.domain.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import app.giftify.wallet.adapter.inbound.web.dto.*;
import app.giftify.support.common.api.response.RsData;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletController 테스트")
class WalletControllerTest {

	@Mock
	private WithdrawWalletUseCase withdrawWalletUseCase;

	@Mock
	private QueryWalletUseCase queryWalletUseCase;

	@Mock
	private QueryWalletHistoryUseCase queryWalletHistoryUseCase;

	@InjectMocks
	private WalletController walletController;

	private static final Long TEST_MEMBER_ID = 100L;

	@Nested
	@DisplayName("withdraw 메서드")
	class WithdrawTests {

		@Test
		@DisplayName("정상적으로 출금 요청한다")
		void withdraw_Success() {
			// given
			WithdrawWalletRequest request = new WithdrawWalletRequest(BigDecimal.valueOf(10000), "088", "1234567890");
			WithdrawWalletResult result = new WithdrawWalletResult(
				1L, TEST_MEMBER_ID, Money.of(90000), Money.of(10000), "WD-100-12345", WithdrawStatus.PENDING
			);
			given(withdrawWalletUseCase.withdraw(any(WithdrawWalletCommand.class))).willReturn(result);

			// when
			ResponseEntity<RsData<WithdrawWalletResponse>> response =
				walletController.withdraw(TEST_MEMBER_ID, request);

			// then
			assertThat(response.getStatusCode().value()).isEqualTo(200);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getResult()).isEqualTo(RsData.Result.SUCCESS);
			assertThat(response.getBody().getData().walletId()).isEqualTo(1L);
			assertThat(response.getBody().getData().status()).isEqualTo("PENDING");
			verify(withdrawWalletUseCase).withdraw(any(WithdrawWalletCommand.class));
		}
	}

	@Nested
	@DisplayName("getBalance 메서드")
	class GetBalanceTests {

		@Test
		@DisplayName("정상적으로 잔액을 조회한다")
		void getBalance_Success() {
			// given
			WalletBalanceResult result = new WalletBalanceResult(1L, TEST_MEMBER_ID, Money.of(50000));
			given(queryWalletUseCase.getBalance(anyLong())).willReturn(result);

			// when
			ResponseEntity<RsData<WalletBalanceResponse>> response =
				walletController.getBalance(TEST_MEMBER_ID);

			// then
			assertThat(response.getStatusCode().value()).isEqualTo(200);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getResult()).isEqualTo(RsData.Result.SUCCESS);
			assertThat(response.getBody().getData().walletId()).isEqualTo(1L);
			assertThat(response.getBody().getData().balance()).isEqualTo(BigDecimal.valueOf(50000));
			verify(queryWalletUseCase).getBalance(TEST_MEMBER_ID);
		}
	}

	@Nested
	@DisplayName("getHistory 메서드")
	class GetHistoryTests {

		@Test
		@DisplayName("정상적으로 거래 내역을 조회한다")
		void getHistory_Success() {
			// given
			WalletHistoryResult historyResult = new WalletHistoryResult(
				1L, TransactionType.CHARGE, Money.of(10000), Money.of(10000),
				"캐시 충전", "charge-123", LocalDateTime.now()
			);
			Page<WalletHistoryResult> result = new PageImpl<>(List.of(historyResult));
			given(queryWalletHistoryUseCase.getHistory(any(WalletHistoryQuery.class))).willReturn(result);

			// when
			ResponseEntity<RsData<Page<WalletHistoryResponse>>> response =
				walletController.getHistory(TEST_MEMBER_ID, null, 0, 20);

			// then
			assertThat(response.getStatusCode().value()).isEqualTo(200);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getResult()).isEqualTo(RsData.Result.SUCCESS);
			assertThat(response.getBody().getData().getContent()).hasSize(1);
			assertThat(response.getBody().getData().getContent().get(0).type()).isEqualTo("CHARGE");
			verify(queryWalletHistoryUseCase).getHistory(any(WalletHistoryQuery.class));
		}

		@Test
		@DisplayName("거래 유형으로 필터링한다")
		void getHistory_WithTypeFilter_Success() {
			// given
			WalletHistoryResult historyResult = new WalletHistoryResult(
				1L, TransactionType.WITHDRAW, Money.of(5000), Money.of(5000),
				"출금", "withdraw-123", LocalDateTime.now()
			);
			Page<WalletHistoryResult> result = new PageImpl<>(List.of(historyResult));
			given(queryWalletHistoryUseCase.getHistory(any(WalletHistoryQuery.class))).willReturn(result);

			// when
			ResponseEntity<RsData<Page<WalletHistoryResponse>>> response =
				walletController.getHistory(TEST_MEMBER_ID, TransactionType.WITHDRAW, 0, 20);

			// then
			assertThat(response.getStatusCode().value()).isEqualTo(200);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getData().getContent().get(0).type()).isEqualTo("WITHDRAW");
		}
	}
}
