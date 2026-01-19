package app.giftify.in.funding;

import app.giftify.domain.funding.FundingStatus;

import java.time.LocalDateTime;

public record FundingCompleteResponseDto(
        Long fundingId,
        Long wishlistItemId,
        FundingStatus status,
        LocalDateTime closeAt
) {
}
