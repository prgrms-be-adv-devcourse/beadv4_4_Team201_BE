package app.giftify.funding.adpater.inbound.dto;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.domain.FundingStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record MyFundingResponseDto(
        // 펀딩 정보
        Long fundingId,
        Long wishlistItemId,
        Integer targetAmount,
        Integer currentAmount,
        FundingStatus status,
        LocalDateTime deadline,

        // ACHIEVED 이후에만 보여짐
        List<FundingParticipantMember> participants,

        // 추가 정보
        double achievementRate,  // 달성률 (%)
        long daysRemaining       // 잔여 일수
        ) {

    public static MyFundingResponseDto fromEntity(Funding funding) {
        double rate = 0.0;
        if (funding.getTargetAmount() > 0) {
            rate = (double) funding.getCurrentAmount() / funding.getTargetAmount() * 100.0;
        }

        long days = 0;
        if (funding.getDeadline() != null) {
            days = ChronoUnit.DAYS.between(LocalDateTime.now(), funding.getDeadline());
            if (days < 0) days = 0;
        }

        return new MyFundingResponseDto(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                funding.getStatus(),
                funding.getDeadline(),
                null,
                Math.round(rate * 10.0) / 10.0,
                days
        );
    }

    public static MyFundingResponseDto fromAchievedFunding(Funding funding, List<FundingParticipantMember> participants) {

        long days = 0;
        if (funding.getDeadline() != null) {
            days = ChronoUnit.DAYS.between(LocalDateTime.now(), funding.getDeadline());
            if (days < 0) days = 0;
        }

        return new MyFundingResponseDto(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                funding.getStatus(),
                funding.getDeadline(),
                participants,
                100.0,
                days
        );
    }
}
