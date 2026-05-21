package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record SettlementPayoutCommand(
	Long settlementId,
	Long sellerId,
	Money amount,
	// 넘겨 받은 이벤트의 id = SettlementCreatedEvent.eventId()
	// 역할: 멱등성 키. Modulith가 같은 이벤트를 재시도할 때,
	//  이미 처리된 이벤트인지 WalletHistory 테이블에서 referenceId + ReferenceType 조합으로 확인하여 중복 지급을 방지
	String referenceId
) {
}
