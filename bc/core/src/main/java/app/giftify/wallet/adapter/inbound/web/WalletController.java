package app.giftify.wallet.adapter.inbound.web;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.adapter.inbound.web.dto.WalletBalanceResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WalletHistoryResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletRequest;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletResponse;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.application.inbound.QueryWalletHistoryUseCase;
import app.giftify.wallet.application.inbound.QueryWalletUseCase;
import app.giftify.wallet.application.inbound.WalletHistoryQuery;
import app.giftify.wallet.application.inbound.WalletHistoryResult;
import app.giftify.wallet.application.inbound.WalletBalanceResult;
import app.giftify.wallet.application.inbound.WithdrawWalletCommand;
import app.giftify.wallet.application.inbound.WithdrawWalletResult;
import app.giftify.wallet.application.inbound.WithdrawWalletUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v2/wallet")
@RequiredArgsConstructor
@Validated
public class WalletController implements WalletV2ApiSpec {
	private final WithdrawWalletUseCase withdrawWalletUseCase;
	private final QueryWalletUseCase queryWalletUseCase;
	private final QueryWalletHistoryUseCase queryWalletHistoryUseCase;

	@Override
	@PostMapping("/withdraw")
	public ResponseEntity<RsData<WithdrawWalletResponse>> withdraw(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody WithdrawWalletRequest request
	) {
		log.info("[WalletController] 출금 요청. memberId={}", memberId);

		WithdrawWalletCommand command = new WithdrawWalletCommand(
			memberId,
			Money.of(request.amount()),
			request.bankCode(),
			request.accountNumber()
		);

		WithdrawWalletResult result = withdrawWalletUseCase.withdraw(command);
		return ResponseEntity.ok(RsData.success(WithdrawWalletResponse.from(result)));
	}

	@Override
	@GetMapping("/balance")
	public ResponseEntity<RsData<WalletBalanceResponse>> getBalance(
		@CurrentMemberId Long memberId
	) {
		log.debug("[WalletController] 잔액 조회. memberId={}", memberId);

		WalletBalanceResult result = queryWalletUseCase.getBalance(memberId);
		return ResponseEntity.ok(RsData.success(WalletBalanceResponse.from(result)));
	}


	@Override
	@GetMapping("/history")
	public ResponseEntity<RsData<Page<WalletHistoryResponse>>> getHistory(
		@CurrentMemberId Long memberId,
		@RequestParam(name = "type", required = false) TransactionType type,
		@RequestParam(name = "page", defaultValue = "0") int page,
		@RequestParam(name = "size", defaultValue = "20") int size
	) {
		log.debug("[WalletController] 거래 내역 조회. memberId={}, type={}, page={}, size={}", memberId, type, page, size);

		WalletHistoryQuery query = new WalletHistoryQuery(memberId, type, page, size);
		Page<WalletHistoryResult> result = queryWalletHistoryUseCase.getHistory(query);
		Page<WalletHistoryResponse> response = result.map(WalletHistoryResponse::from);

		return ResponseEntity.ok(RsData.success(response));
	}
}
