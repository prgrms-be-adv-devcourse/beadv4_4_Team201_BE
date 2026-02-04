package app.giftify.funding.adpater.inbound.dto;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MyFundingSummaryDto(
        Long fundingId,
        Long wishlistItemId,

        FundingStatus status,

        Integer targetAmount,
        Integer currentAmount,

        double achievementRate,
        long daysRemaining
) {
    public static MyFundingSummaryDto fromEntity(Funding funding) {
        double rate = 0.0;
        if (funding.getTargetAmount() > 0) {
            rate = (double) funding.getCurrentAmount() / funding.getTargetAmount() * 100.0;
        }

        long days = 0;
        if (funding.getDeadline() != null) {
            days = ChronoUnit.DAYS.between(LocalDateTime.now(), funding.getDeadline());
            if (days < 0) days = 0;
        }

        return new MyFundingSummaryDto(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getStatus(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                Math.round(rate * 10.0) / 10.0,
                days
        );
    }
}
