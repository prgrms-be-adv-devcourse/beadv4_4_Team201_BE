package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.PlaceOrderForItemCommand;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderSnapshot placeOrderForItem(PlaceOrderForItemCommand command) {
        OrderItem orderItem = OrderItem.create(
                command.targetId(),
                command.targetType(),
                command.sellerId(),
                command.receiverId(),
                command.price(),
                command.amount()
        );

        Order order = Order.create(command.buyerId(), List.of(orderItem), command.method());
        Order savedOrder = orderRepository.save(order);

        return savedOrder.toSnapshot();
    }
}
