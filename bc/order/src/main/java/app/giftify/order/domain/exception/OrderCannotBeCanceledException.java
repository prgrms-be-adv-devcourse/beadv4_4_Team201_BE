package app.giftify.order.domain.exception;

public class OrderCannotBeCanceledException extends OrderDomainException {

    public OrderCannotBeCanceledException(String status) {
        super(OrderErrorCode.ORDER_CANNOT_BE_CANCELED, "현재 주문 상태에서는 취소할 수 없습니다. (status: " + status + ")");
    }
}