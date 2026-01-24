package app.giftify.funding.application.outbound;

import app.giftify.funding.domain.OrderItem;
import java.util.List;

public interface OrderItemRepositoryPort {
    List<OrderItem> saveAll(List<OrderItem> orderItems);
}
