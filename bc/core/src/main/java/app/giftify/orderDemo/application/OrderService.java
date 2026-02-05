package app.giftify.orderDemo.application;

import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.orderDemo.adapter.outbound.client.WishlistClient;
import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.application.inbound.command.CreateOrderItemCommand;
import app.giftify.orderDemo.application.inbound.vo.OrderDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderItemDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.orderDemo.application.inbound.vo.PaymentSnapshot;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OrderSnapshot createOrder(CreateOrderCommand command, List<FundingSnapshot> fundingSnapshots) {
        List<WishlistItemSnapshot> wishlistItemSnapshots = requestWishlistItemSnapshots(command.itemRequests());

        Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap = mapWishlistItemSnapshotByWishlistItemId(wishlistItemSnapshots);
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
    public void markOrderAsPaid(String orderNumber, PaymentSnapshot snapshot) {
        Order order = orderRepository.getByOrderNumber(orderNumber);

        order.toPaid(snapshot.paymentKey(), snapshot.lastTransactionKey(), snapshot.createdAt());
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

    private static void validateWishlistItemSnapshot(WishlistItemSnapshot snapshot) {
        if (snapshot == null) {
            throw new DomainException(OrderErrorCode.WISHLIST_ITEM_SNAPSHOT_NOT_FOUND);
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
            throw new DomainException(OrderErrorCode.WISHLIST_ITEM_SNAPSHOT_NOT_FOUND);
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

    private static Map<Long, WishlistItemSnapshot> mapWishlistItemSnapshotByWishlistItemId(List<WishlistItemSnapshot> wishlistItemSnapshots) {
        return wishlistItemSnapshots.stream()
                .collect(Collectors.toMap(
                        WishlistItemSnapshot::originalWishlistItemId,
                        wishlistItemSnapshot -> wishlistItemSnapshot
                ));
    }

    // todo: wishlist api에서 List로 반환하도록 수정 시 제거 예정
    private List<WishlistItemSnapshot> requestWishlistItemSnapshots(List<PlaceOrderItemRequest> itemRequests) {
        return itemRequests.stream()
                .map(itemRequest -> {
                    WishlistItemSnapshot snapshot = wishlistClient.getWishlistItemSnapshot(itemRequest.wishlistItemId());
                    validateWishlistItemSnapshot(snapshot);
                    return snapshot;
                })
                .toList();
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
