package app.giftify.support.common.event.funding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FundingCreatedEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
}

