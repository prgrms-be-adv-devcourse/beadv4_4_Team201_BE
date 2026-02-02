package app.giftify.wallet.adapter.inbound.web.dto;

import app.giftify.wallet.application.inbound.WalletHistoryResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "지갑 거래 내역")
public record WalletHistoryResponse(
	@Schema(description = "거래 ID", example = "1")
	String id,

	@Schema(description = "거래 유형", example = "CHARGE", allowableValues = {"CHARGE", "WITHDRAW", "PAYMENT"})
	String type,

	@Schema(description = "거래 금액", example = "10000")
	BigDecimal amount,

	@Schema(description = "거래 후 잔액", example = "10000")
	BigDecimal balanceAfter,

	@Schema(description = "거래 설명", example = "캐시 충전")
	String description,

	@Schema(description = "관련 ID (주문번호 등)", example = "CHG-xxx")
	String relatedId,

	@Schema(description = "거래 일시", example = "2025-02-01T10:00:00")
	LocalDateTime createdAt
) {
	public static WalletHistoryResponse from(WalletHistoryResult result) {
		return new WalletHistoryResponse(
			String.valueOf(result.id()),
			result.type().name(),
			result.amount().amount(),
			result.balanceAfter().amount(),
			result.description(),
			result.relatedId(),
			result.createdAt()
		);
	}
}
