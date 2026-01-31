package app.giftify.orderDemo.application.outbound.port;

import app.giftify.orderDemo.domain.Order;

public interface OrderRepository {
    Order save(Order order);
}
