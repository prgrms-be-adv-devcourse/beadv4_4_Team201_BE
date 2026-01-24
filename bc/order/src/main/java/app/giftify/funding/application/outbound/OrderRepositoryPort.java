package app.giftify.funding.application.outbound;

import app.giftify.funding.domain.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
}
