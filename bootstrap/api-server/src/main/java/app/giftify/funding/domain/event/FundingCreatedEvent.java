package app.giftify.funding.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.funding.domain.vo.FundingDetail;

import java.util.List;

/**
 * 펀딩 생성 이벤트 V2
 * - 여러 펀딩 생성을 한 번에 알리기 위한 이벤트
 */
public class FundingCreatedEvent extends BaseDomainEvent {

    private final List<FundingDetail> fundings;

    public FundingCreatedEvent(List<FundingDetail> fundings) {
        super();
        this.fundings = fundings;
    }


    public List<FundingDetail> getFundings() {
        return fundings;
    }

    @Override
    public String toString() {
        return "FundingCreatedEventV2{" +
                " fundings=" + fundings +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
