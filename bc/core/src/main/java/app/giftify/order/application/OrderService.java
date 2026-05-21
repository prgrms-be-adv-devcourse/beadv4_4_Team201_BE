package app.giftify.order.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.order.application.dto.OrderCancelProcessingResult;
import app.giftify.order.application.dto.OrderCancelSummary;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.inbound.command.MarkOrderAsPaidCommand;
import app.giftify.order.application.inbound.command.PlaceOrderCommand;
import app.giftify.order.application.inbound.command.PlaceOrderItemCommand;
import app.giftify.order.application.inbound.vo.OrderDetail;
import app.giftify.order.application.inbound.vo.OrderItemDetail;
import app.giftify.order.application.inbound.vo.OrderSummary;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.shared.domain.port.ProductSnapshotPort;
import app.giftify.order.domain.*;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.*;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.port.FundingQueryPort;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingInfo;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.ProductSnapshot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class OrderService {
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;
    private final OrderCancelProcessor orderCancelProcessor;
    private final FundingQueryPort fundingPort;
    private final ProductSnapshotPort productPort;
    private final TargetTypeResolver targetTypeResolver;
    private final TargetIdResolver targetIdResolver;

    @Transactional
    public OrderSnapshot createOrder(PlaceOrderCommand command) {
        // 1. 필요한 외부 데이터 일괄 조회
        Map<Long, ProductSnapshot> productSnapshots = productPort.getProductSnapshots(command.getProductIds());
        Map<Long, FundingInfo> fundingInfoMap = fetchFundingInfosIfNeeded(command);

        validateRequiredSnapshot(productSnapshots, fundingInfoMap);

        // 2. 주문 아이템 생성
        List<OrderItem> orderItems = createOrderItems(command.items(), productSnapshots, fundingInfoMap);

        // 3. 주문 엔티티 생성 및 저장
        Order order = Order.create(command.buyerId(), orderItems, command.method());
        Order savedOrder = orderRepository.save(order);

        // 4. 스냅샷 변환 및 이벤트 발행
        OrderSnapshot orderSnapshot = savedOrder.toSnapshot();
        handleOrderEvents(orderSnapshot);

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

    @Transactional(readOnly = true)
    public OrderSnapshot getSnapshotByOrderNumber(String orderNumber) {
        return orderRepository.getByOrderNumber(orderNumber).toSnapshot();
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
    public void expire(Long orderId) {
        Order order = orderRepository.getByIdWithItems(orderId);

        order.expired();
    }

    private List<OrderItem> createOrderItems(
            List<PlaceOrderItemCommand> items,
            Map<Long, ProductSnapshot> products,
            Map<Long, FundingInfo> fundings
    ) {
        return items.stream()
                .map(item -> {
                    ProductSnapshot product = getProductSnapshotOrThrow(item.productId(), products);
                    FundingInfo funding = fundings.get(item.wishlistItemId());

                    TargetType targetType = targetTypeResolver.resolve(item.orderItemType(), funding);
                    Long targetId = targetIdResolver.resolve(item, targetType, funding);

                    if (targetId == null) {
                        throw new DomainException(OrderErrorCode.INVALID_TARGET_ID);
                    }

                    return OrderItem.create(
                            targetId,
                            targetType,
                            item.orderItemType(),
                            product.sellerId(),
                            item.receiverId(),
                            Money.of(product.price()),
                            item.amount()
                    );
                })
                .toList();
    }

    private void handleOrderEvents(OrderSnapshot snapshot) {
        publishOrderItemCreatedEventWithoutPendingType(snapshot);
        publishOrderCreatedEvent(snapshot);
    }

    private static ProductSnapshot getProductSnapshotOrThrow(Long productId, Map<Long, ProductSnapshot> productSnapshots) {
        ProductSnapshot productSnapshot = productSnapshots.get(productId);
        if (productSnapshot == null) {
            throw new DomainException(OrderErrorCode.SNAPSHOTS_NOT_FOUND,
                    String.format("상품 정보를 찾을 수 없습니다. productId = %d", productId));
        }

        return productSnapshot;
    }

    private Map<Long, FundingInfo> fetchFundingInfosIfNeeded(PlaceOrderCommand command) {
        if (command.isAllNormalOrder()) {
            return Collections.emptyMap();
        }

        return fundingPort.findFundingInfoByWishlistItemIds(
                command.getWishlistItemIdsByOrderItemType(EnumSet.of(OrderItemType.NORMAL_GIFT, OrderItemType.FUNDING_GIFT))
        );
    }

    private void validateRequiredSnapshot(Map<Long, ProductSnapshot> products, Map<Long, FundingInfo> fundings) {
        // 포트가 null을 반환할 가능성에 대비한 방어 로직
        if (products == null || fundings == null) {
            throw new DomainException(OrderErrorCode.SNAPSHOTS_NOT_FOUND);
        }
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
}
