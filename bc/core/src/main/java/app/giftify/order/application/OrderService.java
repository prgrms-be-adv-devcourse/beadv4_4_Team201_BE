package app.giftify.order.application;

import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderItemRequest;
import app.giftify.order.adapter.outbound.client.WishlistClient;
import app.giftify.order.application.dto.OrderCancelProcessingResult;
import app.giftify.order.application.dto.OrderCancelSummary;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.inbound.command.CreateOrderCommand;
import app.giftify.order.application.inbound.command.CreateOrderItemCommand;
import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.order.application.inbound.vo.OrderDetail;
import app.giftify.order.application.inbound.vo.OrderItemDetail;
import app.giftify.order.application.inbound.vo.OrderSummary;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.*;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.*;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingSnapshot;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;
    private final WishlistClient wishlistClient;
    private final OrderCancelProcessor orderCancelProcessor;

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

        order.paid(command.paymentId(), command.lastTransactionKey());
    }

    @Transactional
    public ResultCode requestCancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.getByIdWithItemsAndLock(orderId);

        validateOwner(memberId, order.getBuyerId());

        if (order.getStatus() == OrderStatus.CANCELING) return ResultCode.IN_PROGRESS;
        if (order.getStatus() == OrderStatus.CANCELED) return ResultCode.ALREADY_PROCESSED;

        order.cancelAll();

        if (order.getStatus() == OrderStatus.CANCELED) return ResultCode.SUCCESS;
        if (order.getStatus() == OrderStatus.CANCELING) {
            eventPublisher.publish(new OrderCancelRequestedEvent(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getPaymentId(),
                    order.getOriginTransactionKey(),
                    order.getTotalAmount()
            ));
            return ResultCode.ACCEPTED;
        }

        throw new PolicyException(
                OrderErrorCode.INVALID_STATUS_CANCEL,
                String.format("주문 전체 취소가 불가능한 주문 상태입니다. status = %s", order.getStatus())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderCancelSummary requestCancelOrderItems(CancelOrderItemsCommand command) {
        Order order = orderRepository.getByIdWithItemsAndLock(command.orderId());

        validateOwner(command.memberId(), order.getBuyerId());
        List<OrderItem> targetItems = order.validatePartialCancelable(command.itemIds());

        OrderCancelProcessingResult processingResult = orderCancelProcessor.process(targetItems);
        Money totalCancelAmount = processingResult.calculateCancelAmount();

        order.synchronizeStatus();

        if (processingResult.hasPendingItems()) {
            eventPublisher.publish(new OrderCancelRequestedEvent(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getPaymentId(),
                    order.getOriginTransactionKey(),
                    totalCancelAmount
            ));
        }

        return OrderCancelSummary.of(processingResult.results(), totalCancelAmount);
    }

    @Transactional
    public void completeCancel(Long orderId) {
        Order order = orderRepository.getByIdWithItemsAndLock(orderId);

        List<OrderItem> cancelingItems = order.getCancelingItems();
        CancelTargetItems targetItems = new CancelTargetItems(cancelingItems);

        targetItems.canceled();
        order.synchronizeStatus();

        eventPublisher.publish(new OrderCanceledEvent(
                orderId,
                targetItems.toSnapshot(order.getBuyerId())
        ));
    }

    @Transactional
    public void failCancel(Long orderId) {
        Order order = orderRepository.getByIdWithItemsAndLock(orderId);

        List<OrderItem> cancelingItems = order.getCancelingItems();
        CancelTargetItems targetItems = new CancelTargetItems(cancelingItems);

        targetItems.failCancel();
        order.synchronizeStatus();
    }

    @Transactional(readOnly = true)
    public Map<Long, Money> calculateTotalAmounts(List<Long> orderIds) {
        if (orderIds.isEmpty()) return Map.of();

        return orderRepository.getAllByIdInWithItems(orderIds).stream()
                .map(order -> {
                    try {
                        return Map.entry(order.getId(), order.getPayableAmount());
                    } catch (BusinessException e) {
                        ErrorCode errorCode = e.getErrorCode();
                        log.error("[금액 집계 건별 실패] orderId: {}, errorCode: {}, message = {}", order.getId(), errorCode.getCode(), errorCode.getMessage(), e);
                        return null;
                    } catch (InfraException e) {
                        InfraErrorCode errorCode = e.getErrorCode();
                        if (errorCode.isRetryable()) throw e;
                        log.error("[금액 집계 건별 실패] orderId: {}, errorCode: {}, message = {}", order.getId(), errorCode.getCode(), errorCode.getMessage(), e);
                        return null;
                    } catch (Exception e) {
                        log.error("[금액 집계 건별 실패] orderId: {}, message = {}", order.getId(), e.getMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull) // 에러 난 건(null)은 제외
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing // 혹시 모를 ID 중복 방어
                ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrderItems(Long orderId, Set<Long> itemIds) {
        Order targetOrder = orderRepository.getByIdWithItemsAndLock(orderId);

        targetOrder.confirmed(itemIds);
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
                itemRequest.orderItemType(),
                itemRequest.wishlistItemId(),
                itemRequest.receiverId(),
                itemRequest.amount(),
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
