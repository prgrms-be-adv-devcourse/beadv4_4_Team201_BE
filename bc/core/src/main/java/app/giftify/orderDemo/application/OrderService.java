package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.PlaceOrderForItemCommand;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;

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

        OrderSnapshot orderSnapshot = savedOrder.toSnapshot();

        orderSnapshot.orderItemSnapshots().stream()
                .filter(item -> item.targetType() == TargetType.FUNDING)
                .forEach(item -> {
                    OrderItemCreatedEvent event = new OrderItemCreatedEvent(
                            item.orderItemId(),
                            item.targetId(),
                            orderSnapshot.orderId(),
                            item.sellerId(),
                            item.price().amount(),
                            item.amount().amount()
                    );
                    eventPublisher.publish(event);
                });

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getOrderNumber(), order.getCreatedAt());
        eventPublisher.publish(event);

        return orderSnapshot;
    }

    @Transactional
    public void assignFundingToOrderItem(Long orderItemId, Long fundingId) {
        OrderItem item = orderItemRepository.getOrderItemById(orderItemId);

        item.updateTargetToFunding(fundingId);  // 이벤트 등록

        // 트랜잭션 커밋 후 이벤트 발행
        item.getOrder().pullEvents().forEach(eventPublisher::publish);
    }
}
