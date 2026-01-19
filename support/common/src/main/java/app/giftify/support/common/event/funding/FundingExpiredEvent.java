package app.giftify.support.common.event.funding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FundingExpiredEvent {
    private final Long fundingId;
}
