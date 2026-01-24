package app.giftify.funding.application.outbound;

import app.giftify.funding.domain.Order;

public interface OrderPaymentPort {
    void initiatePayment(Order order);
}
