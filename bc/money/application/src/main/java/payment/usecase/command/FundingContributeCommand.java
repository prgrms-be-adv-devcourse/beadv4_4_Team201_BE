package payment.usecase.command;

import app.giftify.shared.domain.vo.Money;

/**
 * 펀딩 참여(복합 결제) 커맨드.
 * 예치금 우선 차감 + 부족분 PG 결제 방식을 지원합니다.
 */
public record FundingContributeCommand(
	Long userId,
	Money amount
) {
}
