package app.giftify.funding.adapter.inbound.dto;

import java.util.List;

public record FundingStartRequest(
        Long participantId,
        List<FundingItemRequest> items
) {
}
