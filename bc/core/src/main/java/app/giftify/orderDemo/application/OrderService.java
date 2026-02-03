package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.vo.OrderDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderItemDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.orderDemo.domain.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service("orderV2Service")
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderSnapshot createOrder(CreateOrderCommand command) {
        List<OrderItem> orderItems = createOrderItems(command);

        Order order = Order.create(command.buyerId(), orderItems, command.method());
        Order savedOrder = orderRepository.save(order);

        OrderSnapshot orderSnapshot = savedOrder.toSnapshot();

        publishOrderItemCreatedEventWithoutPendingType(orderSnapshot);
        publishOrderCreatedEvent(orderSnapshot);

        return orderSnapshot;
    }

    private void publishOrderCreatedEvent(OrderSnapshot orderSnapshot) {
        OrderCreatedEvent event = new OrderCreatedEvent(orderSnapshot.orderId(), orderSnapshot.orderNumber(), orderSnapshot.createdAt());
        eventPublisher.publish(event);
    }

    private void publishOrderItemCreatedEventWithoutPendingType(OrderSnapshot orderSnapshot) {
        orderSnapshot.orderItemSnapshots().stream()
                .filter(item -> item.targetType() != TargetType.FUNDING_PENDING)
                .forEach(item -> {
                    OrderItemCreatedEvent event = new OrderItemCreatedEvent(
                            item.orderItemId(),
                            item.targetId(),
                            item.targetType(),
                            item.orderItemType(),
                            orderSnapshot.orderId(),
                            item.sellerId(),
                            item.price(),
                            item.amount()
                    );
                    eventPublisher.publish(event);
                });
    }

    private static @NonNull List<OrderItem> createOrderItems(CreateOrderCommand command) {
        return command.items().stream()
                .map(item -> OrderItem.create(
                        item.targetId(),
                        item.targetType(),
                        item.orderItemType(),
                        item.sellerId(),
                        item.receiverId(),
                        item.price(),
                        item.amount()))
                .toList();
    }

    @Transactional
    public void assignFundingToOrderItem(Long orderItemId, Long fundingId) {
        OrderItem item = orderItemRepository.getOrderItemById(orderItemId);

        item.updateTargetToFunding(fundingId);  // 이벤트 등록

        // 트랜잭션 커밋 후 이벤트 발행
        item.getOrder().pullEvents().forEach(eventPublisher::publish);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> getOrders(Long memberId, Pageable pageable) {
        Page<Order> pages = orderRepository.getByBuyerId(memberId, pageable);

        return pages.map(OrderSummary::of);
    }

    @Transactional(readOnly = true)
    public OrderDetail getOrderDetail(Long memberId, Long orderId) {
        Order order = orderRepository.getById(orderId);

        validateOwner(memberId, order.getId());

        OrderSummary summary = OrderSummary.of(order);
        List<OrderItemDetail> itemDetails = getOrderItemDetails(order);

        return new OrderDetail(summary, itemDetails);
    }

    private static void validateOwner(Long memberId, Long orderId) {
        if (!Objects.equals(orderId, memberId)) {
            throw new PolicyException(
                    OrderErrorCode.ORDER_OWNER_MISMATCH,
                    String.format("memberId = %d, orderId = %d", memberId, orderId)
            );
        }
    }

    private static @NonNull List<OrderItemDetail> getOrderItemDetails(Order order) {
        return order.getItems().stream()
                .map(OrderItemDetail::of)
                .toList();
    }
}
