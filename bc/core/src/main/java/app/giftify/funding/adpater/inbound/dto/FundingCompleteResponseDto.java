package app.giftify.funding.adpater.inbound.dto;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;

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
               funding.getWishlistItemId(),
               funding.getStatus(),
               funding.getClosedAt()
       );
   }
}
