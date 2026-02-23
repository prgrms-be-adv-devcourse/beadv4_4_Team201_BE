package app.giftify.orderDemo.application.outbound.port;

import app.giftify.orderDemo.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);

    Page<Order> getByBuyerId(Long buyerId, Pageable pageable);

    Order getById(Long orderId);

    Order getByOrderNumber(String orderNumber);

    Order getByIdWithItemsAndLock(Long id);

    List<Order> getAllByIdInWithItems(List<Long> ids);
}
