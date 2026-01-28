package app.giftify.in.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingStatus;

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
