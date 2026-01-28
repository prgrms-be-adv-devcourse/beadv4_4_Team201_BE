package app.giftify.order.application.outbound;

import app.giftify.order.domain.Order;

public interface OrderPaymentPort {
    void initiatePayment(Order order);

    void cancelPayment(String orderNumber);
}
