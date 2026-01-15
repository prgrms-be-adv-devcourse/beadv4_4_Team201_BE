package app.giftify.support.common.event.funding;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 펀딩 생성 이벤트
 * Funding BC에서 발행하고, Member BC에서 수신하여 WishlistItem 상태를 변경합니다.
 */
@Getter
public class FundingCreatedEvent extends ApplicationEvent {
    private final Long fundingId;
    private final Long wishlistItemId;

    public FundingCreatedEvent(Object source, Long fundingId, Long wishlistItemId) {
        super(source);
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
    }
}

