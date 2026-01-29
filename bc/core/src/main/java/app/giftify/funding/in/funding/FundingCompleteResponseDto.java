package app.giftify.funding.in.funding;

import app.giftify.funding.domain.funding.Funding;
import app.giftify.funding.domain.funding.FundingStatus;

import java.time.LocalDateTime;


public record FundingCompleteResponseDto(
        Long fundingId,
        Long wishlistItemId,
        FundingStatus status,
        LocalDateTime closeAt
) {

   public static FundingCompleteResponseDto fromEntity(Funding funding) {
       return new FundingCompleteResponseDto(
               funding.getId(),
               funding.getFundingWishlistItem().getId(),
               funding.getStatus(),
               funding.getClosedAt()
       );
   }
}
