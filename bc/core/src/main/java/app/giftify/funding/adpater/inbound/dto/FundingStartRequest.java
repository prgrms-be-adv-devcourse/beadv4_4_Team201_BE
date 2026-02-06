package app.giftify.funding.adpater.inbound.dto;

import java.util.List;

public record FundingStartRequest(
        Long participantId,
        List<FundingItemRequest> items
) {
}
