package app.giftify.auth.adapter.outbound.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Wallet Internal API 클라이언트.
 * Auth 모듈에서 Wallet 모듈의 내부 API를 호출할 때 사용합니다.
 */
public interface WalletApiClient {

    @PostExchange("/api/internal/wallets")
    CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

    record CreateWalletRequest(Long memberId) {}

    record CreateWalletResponse(Long walletId, Long memberId, boolean created) {}
}
