package app.giftify.order.domain.exception;

public class OrderNotPayableException extends OrderDomainException {

    public OrderNotPayableException(String status) {
        super(OrderErrorCode.ORDER_NOT_PAYABLE, "결제가 불가능한 주문 상태입니다. (status: " + status + ")");
    }
}