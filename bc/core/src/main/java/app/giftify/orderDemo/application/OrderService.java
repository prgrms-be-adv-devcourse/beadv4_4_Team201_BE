package app.giftify.orderDemo.application;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.orderDemo.adapter.outbound.client.WishlistClient;
import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.command.CreateOrderItemCommand;
import app.giftify.orderDemo.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.orderDemo.application.inbound.vo.OrderDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderItemDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.CancelTargetItems;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("orderV2Service")
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;
    private final WishlistClient wishlistClient;

    @Transactional
    public OrderSnapshot createOrder(@Valid CreateOrderCommand command, List<FundingSnapshot> fundingSnapshots) {
        Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap = requestWishlistItemSnapshots(command.itemRequests());

        Map<Long, Long> fundingIdMap = mapFundingIdByWishlistItemId(fundingSnapshots);

        List<CreateOrderItemCommand> orderItemCommands = toOrderItemCommands(command, wishlistItemSnapshotMap, fundingIdMap);
        List<OrderItem> orderItems = createOrderItems(orderItemCommands);

        Order order = Order.create(command.buyerId(), orderItems, command.method());
        Order savedOrder = orderRepository.save(order);

        OrderSnapshot orderSnapshot = savedOrder.toSnapshot();

        publishOrderItemCreatedEventWithoutPendingType(orderSnapshot);
        publishOrderCreatedEvent(orderSnapshot);

        return orderSnapshot;
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

        validateOwner(memberId, order.getBuyerId());

        OrderSummary summary = OrderSummary.of(order);
        List<OrderItemDetail> itemDetails = getOrderItemDetails(order);

        return new OrderDetail(summary, itemDetails);
    }

    @Transactional
    public void markOrderAsPaid(@Valid MarkOrderAsPaidCommand command) {
        Order order = orderRepository.getByOrderNumber(command.orderNumber());

        order.toPaid(command.paymentKey(), command.lastTransactionKey());
    }

    @Retryable(
            retryFor = { InfraException.class },
            exceptionExpression = "@retryService.isRetryable(#root)",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @Transactional
    public void requestCancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.getByIdWithLock(orderId);

        validateOwner(memberId, order.getBuyerId());

        List<OrderItem> cancelableItems = orderItemRepository.getCancelableItemsByOrderId(orderId);
        CancelTargetItems targetItems = new CancelTargetItems(cancelableItems);

        Money cancelAmount = targetItems.calculateCancelAmount();

        LocalDateTime cancelRequestedAt = LocalDateTime.now();

        order.pendingToCancel(cancelRequestedAt);
        targetItems.pendingToCancel(cancelRequestedAt);

        eventPublisher.publish(new OrderCancelRequestedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getPaymentKey(),
                order.getOriginTransactionKey(),
                cancelAmount
        ));
    }

    private static void validateOwner(Long memberId, Long buyerId) {
        if (!Objects.equals(buyerId, memberId)) {
            throw new PolicyException(
                    OrderErrorCode.ORDER_OWNER_MISMATCH,
                    String.format("memberId = %d, orderId = %d", memberId, buyerId)
            );
        }
    }

    private static @NonNull List<OrderItemDetail> getOrderItemDetails(Order order) {
        return order.getItems().stream()
                .map(OrderItemDetail::of)
                .toList();
    }

    private static void validateSnapshots(Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap) {
        if (wishlistItemSnapshotMap == null || wishlistItemSnapshotMap.isEmpty()) {
            throw new DomainException(OrderErrorCode.SNAPSHOTS_NOT_FOUND);
        }
    }

    private static @NonNull List<CreateOrderItemCommand> toOrderItemCommands(CreateOrderCommand command, Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap, Map<Long, Long> fundingIdMap) {

        return command.itemRequests().stream()
                .map(itemRequest -> generateOrderItemCommand(wishlistItemSnapshotMap, fundingIdMap, itemRequest))
                .toList();
    }

    private static CreateOrderItemCommand generateOrderItemCommand(Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap, Map<Long, Long> fundingIdMap, PlaceOrderItemRequest itemRequest) {
        WishlistItemSnapshot wishlistItemSnapshot = getWishlistItemSnapshot(wishlistItemSnapshotMap, itemRequest);
        return CreateOrderItemCommand.of(
                itemRequest,
                wishlistItemSnapshot,
                fundingIdMap.getOrDefault(itemRequest.wishlistItemId(), null)
        );
    }

    private static WishlistItemSnapshot getWishlistItemSnapshot(Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap, PlaceOrderItemRequest itemRequest) {
        if (!wishlistItemSnapshotMap.containsKey(itemRequest.wishlistItemId()))
            throw new DomainException(OrderErrorCode.SNAPSHOTS_NOT_FOUND);
        else
            return wishlistItemSnapshotMap.get(itemRequest.wishlistItemId());
    }

    private static Map<Long, Long> mapFundingIdByWishlistItemId(List<FundingSnapshot> fundingSnapshots) {
        return fundingSnapshots.stream()
                .collect(Collectors.toMap(
                        FundingSnapshot::wishlistItemId, // Key 추출
                        FundingSnapshot::fundingId
                ));
    }

    private Map<Long, WishlistItemSnapshot> requestWishlistItemSnapshots(List<PlaceOrderItemRequest> itemRequests) {
        List<Long> wishlistItemIds = itemRequests.stream()
                .map(PlaceOrderItemRequest::wishlistItemId).toList();


        Map<Long, WishlistItemSnapshot> snapshotList = wishlistClient.getSnapshotList(wishlistItemIds);

        validateSnapshots(snapshotList);

        return snapshotList;
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

    private static @NonNull List<OrderItem> createOrderItems(List<CreateOrderItemCommand> commands) {
        return commands.stream()
                .map(command -> OrderItem.create(
                        command.targetId(),
                        command.targetType(),
                        command.orderItemType(),
                        command.sellerId(),
                        command.receiverId(),
                        command.price(),
                        command.amount())
                )
                .toList();
    }
}
