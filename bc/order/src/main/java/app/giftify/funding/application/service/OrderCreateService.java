package app.giftify.funding.application.service;

import app.giftify.funding.adapter.inbound.web.dto.request.OrderCreateRequest;
import app.giftify.funding.adapter.inbound.web.dto.response.OrderResponse;
import app.giftify.funding.application.inbound.OrderCreateUseCase;
import app.giftify.funding.application.outbound.OrderItemRepositoryPort;
import app.giftify.funding.application.outbound.OrderPaymentPort;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.OrderItem;
import app.giftify.funding.domain.OrderStatus;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import app.giftify.funding.domain.exception.OrderErrorCode;
import app.giftify.funding.domain.exception.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCreateService implements OrderCreateUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderItemRepositoryPort orderItemRepositoryPort;
    private final OrderPaymentPort orderPaymentPort;

    @Override
    public OrderResponse createOrder(OrderCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        // 총 주문 금액 계산
        Money totalAmount = request.items().stream()
                .map(item -> Money.of(item.price()).amount().multiply(java.math.BigDecimal.valueOf(item.quantity())))
                .map(Money::new)
                .reduce(Money.zero(), Money::plus);

        // Order 생성 및 저장
        Order order = Order.builder()
                .orderNumber(Order.generateOrderNumber())
                .buyerId(request.buyerId())
                .totalAmount(totalAmount)
                .paymentMethod(request.paymentMethod())
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(now)
                .build();
        
        Order savedOrder = orderRepositoryPort.save(order);

        if (savedOrder == null || savedOrder.getId() == null) {
            throw new OrderException(OrderErrorCode.INTERNAL_SERVER_ERROR, "주문 생성에 실패했습니다.");
        }

        // OrderItem 생성 및 저장
        List<OrderItem> orderItems = request.items().stream()
                .map(itemReq -> OrderItem.builder()
                        .orderId(savedOrder.getId())
                        .targetSnapshotId(itemReq.targetSnapshotId())
                        .targetType(itemReq.targetType())
                        .sellerId(itemReq.sellerId())
                        .receiverId(itemReq.receiverId())
                        .price(Money.of(itemReq.price()))
                        .quantity(new Quantity(itemReq.quantity()))
                        .createdAt(now)
                        .build())
                .toList();

        List<OrderItem> savedItems = orderItemRepositoryPort.saveAll(orderItems);

        if (savedItems == null || savedItems.isEmpty()) {
            throw new OrderException(OrderErrorCode.INTERNAL_SERVER_ERROR, "주문 아이템 생성에 실패했습니다.");
        }

        log.info("주문 생성이 완료되었습니다. [주문 ID: {}]", savedOrder.getId());

        // 결제 요청 (bc:money:payment API 호출)
        orderPaymentPort.initiatePayment(savedOrder);

        // 응답 반환
        List<OrderResponse.OrderItemResponse> itemResponses = savedItems.stream()
                .map(OrderResponse.OrderItemResponse::from)
                .toList();

        return OrderResponse.from(savedOrder, itemResponses);
    }

    @Override
    public String confirmOrder(Long orderId, Long memberId) {
        Order order = orderRepositoryPort.findByIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));
        String previousStatus = order.getStatus().toString();

        order.toConfirmed();
        orderRepositoryPort.save(order);

        return previousStatus;
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long memberId) {
        Order order = orderRepositoryPort.findByIdAndBuyerId(orderId, memberId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다."));

        // 도메인 로직을 통한 상태 변경 (이미 확정된 주문인지 체크 포함)
        order.toCancelled();
        Order savedOrder = orderRepositoryPort.save(order);

        // 관련 주문 아이템들도 취소 처리
        List<OrderItem> items = orderItemRepositoryPort.findByOrderId(orderId);
        orderItemRepositoryPort.saveAll(items);

        // 결제 취소 및 환불 API 호출
        orderPaymentPort.cancelPayment(savedOrder.getOrderNumber());

        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(OrderResponse.OrderItemResponse::from)
                .toList();

        return OrderResponse.from(savedOrder, itemResponses);
    }
}
