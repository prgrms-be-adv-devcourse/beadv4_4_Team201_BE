package app.giftify.order.domain.exception;

import app.giftify.order.domain.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderExceptionTest {

    @Test
    @DisplayName("FundingEventMismatchException 필드 검증")
    void fundingEventMismatchException_Test() {
        FundingEventMismatchException exception = new FundingEventMismatchException(1L);
        assertEquals(OrderErrorCode.FUNDING_EVENT_MISMATCH, exception.getErrorCode());
        assertEquals("펀딩 이벤트와 주문 정보가 일치하지 않습니다. (fundingId: 1)", exception.getMessage());
    }

    @Test
    @DisplayName("OrderCannotBeCanceledException 필드 검증")
    void orderCannotBeCanceledException_Test() {
        OrderCannotBeCanceledException exception = new OrderCannotBeCanceledException(OrderStatus.CONFIRMED.name());
        assertEquals(OrderErrorCode.ORDER_CANNOT_BE_CANCELED, exception.getErrorCode());
        assertEquals("현재 주문 상태에서는 취소할 수 없습니다. (status: CONFIRMED)", exception.getMessage());
    }

    @Test
    @DisplayName("OrderNotPayableException 필드 검증")
    void orderNotPayableException_Test() {
        OrderNotPayableException exception = new OrderNotPayableException(OrderStatus.CANCELED.name());
        assertEquals(OrderErrorCode.ORDER_NOT_PAYABLE, exception.getErrorCode());
        assertEquals("결제가 불가능한 주문 상태입니다. (status: CANCELED)", exception.getMessage());
    }

    @Test
    @DisplayName("PaymentAlreadyCompletedException 필드 검증")
    void paymentAlreadyCompletedException_Test() {
        PaymentAlreadyCompletedException exception = new PaymentAlreadyCompletedException();
        assertEquals(OrderErrorCode.PAYMENT_KEY_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("OrderErrorCode Getter 검증")
    void orderErrorCode_Test() {
        OrderErrorCode errorCode = OrderErrorCode.ORDER_NOT_FOUND;
        assertEquals("O101", errorCode.getCode());
        assertEquals("주문을 찾을 수 없습니다.", errorCode.getMessage());
    }
}
