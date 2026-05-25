package app.giftify.order.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.order.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.inbound.command.ConfirmFundingOrderCommand;
import app.giftify.order.application.outbound.port.OrderItemRepository;
import app.giftify.order.application.outbound.port.OrderRepository;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.support.common.api.exception.*;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.order.domain.event.OrderConfirmPendingEvent;
import app.giftify.order.domain.vo.ConfirmItem;
import app.giftify.support.common.money.Money;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FundingOrderService {
	private static final Logger log = LoggerFactory.getLogger(FundingOrderService.class);

    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final EventPublisher eventPublisher;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public void requestCancelFundingOrder(CancelFundingOrderCommand command) {
        Long fundingId = command.fundingId();

        List<OrderItem> orderItems = orderItemRepository.getOrderItemsWithOrderAndFindingId(fundingId);

        validateItemsNotEmpty(command.fundingId(), orderItems);
        validateTotalAmountMatch(command.expiredAmount(), orderItems);

        Map<Order, List<Long>> orderItemsByOrderId = groupOrderItemsByOrder(orderItems);

        orderItemsByOrderId.forEach((order, itemIds) -> {
            try {
                orderService.requestCancelOrderItems(new CancelOrderItemsCommand(
                        order.getId(),
                        order.getBuyerId(),
                        itemIds
                ));
            } catch (BusinessException e) {
                ErrorCode errorCode = e.getErrorCode();
                log.error("[펀딩 만료 주문 취소 실패] 비즈니스 예외 발생 fundingId: {}, orderId: {}, errorCode: {}, message: {}", fundingId, order.getId(), errorCode.getCode(), errorCode.getMessage(), e);
            } catch (InfraException e) {
                InfraErrorCode errorCode = e.getErrorCode();
                log.error("[펀딩 만료 주문 취소 실패] 인프라 예외 발생 fundingId: {}, orderId: {}, errorCode: {}, message: {}", fundingId, order.getId(), errorCode.getCode(), errorCode.getMessage(), e);
            } catch (Exception e) {
                log.error("[펀딩 만료 주문 취소 실패] 알 수 없는 오류 fundingId: {}, orderId: {}", fundingId, order.getId(), e);
            }
        });
    }

    @Transactional
    public void confirmOrderItemsByFunding(ConfirmFundingOrderCommand command) {
        eventPublisher.publish(
                new OrderConfirmPendingEvent(
                        List.of(ConfirmItem.ofFunding(command.productId()))
                )
        );

        Map<Long, List<Long>> orderIdMap = orderItemRepository.getItemIdMapByFundingId(command.fundingId());

        List<Long> orderItemIds = orderIdMap.values().stream()
                .flatMap(List::stream)
                .toList();

        int updatedItems = orderItemRepository.confirmOrderItems(orderItemIds);

        if (updatedItems != orderItemIds.size()) {
            log.error("[주문 확정 실패] 주문 아이템 중 확정할 수 없는 상태의 아이템이 포함되어 있습니다. Expected Count: {}, Updated Count: {}, OrderItem IDs : {}",
                    orderItemIds.size(), updatedItems, orderItemIds);

            throw new DomainException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        List<Order> orders = orderRepository.getAllByIdInWithItems(new ArrayList<>(orderIdMap.keySet()));
        orders.forEach(Order::synchronizeStatus);
    }

    private static void validateItemsNotEmpty(Long fundingId, List<OrderItem> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new PolicyException(
                    OrderErrorCode.ORDER_ITEM_NOT_FOUND,
                    String.format("만료된 펀딩과 매칭되는 주문 아이템이 존재하지 않습니다. fundingId = %d", fundingId)
            );
        }
    }

    private static @NonNull Map<Order, List<Long>> groupOrderItemsByOrder(List<OrderItem> orderItems) {
        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        OrderItem::getOrder, // Key: 주문 ID
                        Collectors.mapping(OrderItem::getId, Collectors.toList()) // Value: 주문 아이템 ID 리스트
                ));
    }

    private static void validateTotalAmountMatch(Money fundingAmount, List<OrderItem> orderItems) {
        Money totalAmount = orderItems.stream()
                .map(OrderItem::getAmount)
                .reduce(Money.zero(), Money::plus);

        if (!fundingAmount.equals(totalAmount)) {
            throw new DomainException(
                    OrderErrorCode.CANCEL_AMOUNT_MISMATCH,
                    String.format("펀딩 금액과 주문 아이템 총합이 일치하지 않습니다. fundingAmount = %s, totalAmount = %s", fundingAmount.toPlainString(), totalAmount.amount().toPlainString())
            );
        }
    }
}