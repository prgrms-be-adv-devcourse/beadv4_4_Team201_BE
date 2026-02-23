package app.giftify.orderDemo.application;

import app.giftify.orderDemo.application.inbound.command.CancelFundingOrderCommand;
import app.giftify.orderDemo.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.orderDemo.application.outbound.port.OrderItemRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.orderDemo.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.*;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundingOrderCancelService {
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

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