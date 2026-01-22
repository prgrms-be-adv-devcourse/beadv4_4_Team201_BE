package app.giftify.order.domain.exception;

public class FundingEventMismatchException extends OrderDomainException {

    public FundingEventMismatchException(Long fundingId) {
        super(OrderErrorCode.FUNDING_EVENT_MISMATCH, "펀딩 이벤트와 주문 정보가 일치하지 않습니다. (fundingId: " + fundingId + ")");
    }
}