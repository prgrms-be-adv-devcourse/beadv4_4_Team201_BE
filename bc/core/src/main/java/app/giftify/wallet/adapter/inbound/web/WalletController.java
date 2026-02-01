package app.giftify.wallet.adapter.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.CommonResponse;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.adapter.inbound.web.dto.ChargeWalletRequest;
import app.giftify.wallet.adapter.inbound.web.dto.ChargeWalletResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WalletBalanceResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletRequest;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletResponse;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletResult;
import app.giftify.wallet.application.inbound.ChargeWalletUseCase;
import app.giftify.wallet.application.inbound.QueryWalletUseCase;
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
public class WalletController {
	private final ChargeWalletUseCase chargeWalletUseCase;
	private final WithdrawWalletUseCase withdrawWalletUseCase;
	private final QueryWalletUseCase queryWalletUseCase;

	/**
	 * 지갑 충전 (Toss PG 결제 완료 후 호출)
	 */
	@Deprecated
	@PostMapping("/charge")
	public ResponseEntity<CommonResponse<ChargeWalletResponse>> charge(
		@CurrentMemberId Long memberId,
		@Valid @RequestBody ChargeWalletRequest request
	) {
		log.info("[WalletController] 충전 요청. memberId={}", memberId);

		ChargeWalletCommand command = new ChargeWalletCommand(
			memberId,
			Money.of(request.amount()),
			request.chargeOrderId()
		);

		ChargeWalletResult result = chargeWalletUseCase.charge(command);
		return ResponseEntity.ok(CommonResponse.success(ChargeWalletResponse.from(result)));
	}

	/**
	 * 지갑 출금
	 */
	@PostMapping("/withdraw")
	public ResponseEntity<CommonResponse<WithdrawWalletResponse>> withdraw(
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
		return ResponseEntity.ok(CommonResponse.success(WithdrawWalletResponse.from(result)));
	}

	/**
	 * 잔액 조회
	 */
	@GetMapping("/balance")
	public ResponseEntity<CommonResponse<WalletBalanceResponse>> getBalance(
		@CurrentMemberId Long memberId
	) {
		log.debug("[WalletController] 잔액 조회. memberId={}", memberId);

		WalletBalanceResult result = queryWalletUseCase.getBalance(memberId);
		return ResponseEntity.ok(CommonResponse.success(WalletBalanceResponse.from(result)));
	}
}
