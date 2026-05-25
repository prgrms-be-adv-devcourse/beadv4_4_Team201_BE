package app.giftify.funding.adapter.inbound.dto;

import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.domain.type.FundingStatus;

import java.time.LocalDateTime;


public record FundingCompleteResponseDto(
        Long fundingId,
        Long wishlistItemId,
        String productName,
        FundingStatus status,
        LocalDateTime closeAt
) {

   public static FundingCompleteResponseDto fromEntity(Funding funding) {
       return new FundingCompleteResponseDto(
               funding.getId(),
               funding.getWishlistItemId(),
               funding.getProductName(),
               funding.getStatus(),
               funding.getClosedAt()
       );
   }
}
