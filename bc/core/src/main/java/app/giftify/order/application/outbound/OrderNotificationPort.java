package app.giftify.order.application.outbound;

import app.giftify.order.domain.Order;

public interface OrderNotificationPort {
    void notifyOrderTimeout(Order order);
}
