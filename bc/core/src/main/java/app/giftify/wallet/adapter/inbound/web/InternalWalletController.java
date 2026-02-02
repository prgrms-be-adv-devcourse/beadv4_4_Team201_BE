package app.giftify.wallet.adapter.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.wallet.application.inbound.CreateWalletUseCase;
import app.giftify.wallet.application.inbound.CreateWalletUseCase.CreateWalletResult;
import lombok.RequiredArgsConstructor;

/**
 * 내부 서비스 간 통신을 위한 Wallet API
 */
@RestController
@RequestMapping("/api/internal/wallets")
@RequiredArgsConstructor
public class InternalWalletController {

	private final CreateWalletUseCase createWalletUseCase;

	/**
	 * 회원의 지갑을 생성합니다.
	 * 이미 존재하는 경우 기존 지갑 정보를 반환합니다.
	 */
	@PostMapping
	public ResponseEntity<CreateWalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
		CreateWalletResult result = createWalletUseCase.createIfNotExists(request.memberId());
		
		int status = result.created() ? 201 : 200;
		return ResponseEntity.status(status).body(
			new CreateWalletResponse(result.walletId(), result.memberId(), result.created())
		);
	}

	public record CreateWalletRequest(Long memberId) {}
	
	public record CreateWalletResponse(Long walletId, Long memberId, boolean created) {}
}
