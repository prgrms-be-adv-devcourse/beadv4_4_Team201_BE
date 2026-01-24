package app.giftify.funding.application.outbound;

import app.giftify.funding.domain.Order;

public interface OrderNotificationPort {
    void notifyOrderTimeout(Order order);
}
