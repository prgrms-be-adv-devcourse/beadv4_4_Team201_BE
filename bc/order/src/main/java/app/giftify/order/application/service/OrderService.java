package app.giftify.order.application.service;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.order.domain.domain.OrderItem;
import app.giftify.order.domain.domain.OrderStatus;
import app.giftify.order.domain.event.OrderCreatedEvent;
import app.giftify.order.domain.event.OrderItemConfirmedEvent;
import app.giftify.order.domain.event.OrderPaidEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 주문 관련 비즈니스 로직을 처리
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final EventPublisher eventPublisher;

    private static final int AUTO_CANCEL_MINUTES = 30;

    // 주문 생성 (펀딩 참여 시 호출)
    // 1. OrderItem 목록 생성
    // 2. 총 결제 금액 계산
    // 3. Order 생성 및 저장
    // 4. OrderCreatedEvent 발행
    @Override
    public Order createOrder(CreateOrderCommand command) {
        List<OrderItem> orderItems = command.orderItems().stream()
                .map(itemCommand -> OrderItem.builder()
                        .id(null)
                        .fundingId(itemCommand.fundingId())
                        .productId(itemCommand.productId())
                        .sellerId(itemCommand.sellerId())
                        .receiverId(itemCommand.receiverId())
                        .price(itemCommand.price())
                        .quantity(itemCommand.quantity())
                        .status(OrderStatus.PAYMENT_PENDING)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        Money totalAmount = command.orderItems().stream()
                .map(item -> (Money) item.price().times(item.quantity().getValue()))
                .reduce(Money.zero(), Money::plus);

        Order order = Order.builder()
                .id(null)
                .orderNumber(Order.generateOrderNumber())
                .buyerId(command.buyerId())
                .totalAmount(totalAmount)
                .status(OrderStatus.PAYMENT_PENDING)
                .createdAt(LocalDateTime.now())
                .orderItems(orderItems)
                .build();

        Order savedOrder = orderRepositoryPort.save(order);

        // TODO: Redis TTL 설정 (AUTO_CANCEL_MINUTES 분 뒤에 만료되도록)
        // redisTemplate.opsForValue().set("ORDER_PENDING:" + savedOrder.getId(), "PENDING", Duration.ofMinutes(AUTO_CANCEL_MINUTES));

        // 주문 생성 이벤트 발행
        eventPublisher.publish(new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getBuyerId(),
                savedOrder.getOrderItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemInfo(
                                item.getId(),
                                item.getFundingId(),
                                item.getProductId(),
                                item.getReceiverId()
                        ))
                        .collect(Collectors.toList())
        ));

        return savedOrder;
    }

    // 결제 완료 처리
    // 1. 비관적 락을 사용하여 주문 조회 (자동 취소와의 경합 방지)
    // 2. 주문 상태를 ORDERED로 변경 (하위 아이템 포함)
    // 3. OrderPaidEvent 발행 (펀딩 참여 확정 트리거)
    @Override
    public void payOrder(PayOrderCommand command) {
        // 비관적 락을 사용하여 자동 취소와 동시에 일어나는 것을 방지
        Order order = orderRepositoryPort.findByIdWithLock(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문은 결제할 수 없습니다.");
        }

        order.toOrdered(command.paymentKey());
        orderRepositoryPort.save(order);

        // TODO: Redis TTL 키 삭제 (결제 완료되었으므로 자동 취소 대상 아님)
        // redisTemplate.delete("ORDER_PENDING:" + order.getId());

        // 결제 완료 이벤트 발행
        eventPublisher.publish(new OrderPaidEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getPaymentKey()
        ));
    }

    // 주문 취소 처리
    // 1. 비관적 락을 사용하여 주문 조회 (결제 완료와의 경합 방지)
    // 2. 주문 상태를 CANCELED로 변경 (하위 아이템 포함)
    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderRepositoryPort.findByIdWithLock(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() == OrderStatus.ORDERED) {
            // 결제 후 취소인 경우 환불 로직 연동 가능
            log.info("결제 완료된 주문 취소 시도: {}", order.getOrderNumber());
        }

        order.toCancelled();
        orderRepositoryPort.save(order);
    }

    // 주문 아이템 확정 처리
    // 1. 수령자 본인 여부 확인
    // 2. 아이템 상태를 CONFIRMED로 변경
    // 3. 모든 아이템 확정 시 주문 상태도 CONFIRMED로 변경 (상향식 전이)
    // 4. OrderItemConfirmedEvent 발행 (정산 생성 트리거)
    @Override
    public void confirmOrderItem(ConfirmOrderItemCommand command) {
        Order order = orderRepositoryPort.findById(command.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderItem targetItem = order.getOrderItems().stream()
                .filter(item -> item.getId().equals(command.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문 아이템을 찾을 수 없습니다."));

        if (!targetItem.getReceiverId().equals(command.receiverId())) {
            throw new IllegalArgumentException("수령자만 구매 확정을 할 수 있습니다.");
        }

        targetItem.toConfirmed();
        order.checkAllItemsConfirmed();

        orderRepositoryPort.save(order);

        // 확정 이벤트 발행 (정산 트리거)
        eventPublisher.publish(new OrderItemConfirmedEvent(
                order.getId(),
                targetItem.getId(),
                targetItem.getSellerId(),
                targetItem.getReceiverId(),
                targetItem.getPrice(),
                targetItem.getQuantity().getValue()
        ));
    }

    // 결제가 안 된 주문 자동 취소 스케줄러 (또는 Redis KeyExpiration 이벤트 리스너에서 호출)
    // 1분마다 실행되어 유효 시간이 경과한 주문들을 취소 처리
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPendingOrders() {
        log.info("결제 대기 중인 주문 자동 취소 스케줄러 실행");
        List<Order> expiredOrders = orderRepositoryPort.findPaymentPendingOrdersOlderThan(AUTO_CANCEL_MINUTES);

        for (Order order : expiredOrders) {
            try {
                // 개별 건별로 트랜잭션 분리나 락 고려 가능 (이미 find에서 걸러졌으나 안전을 위해 서비스 메서드 호출 권장)
                cancelOrder(new CancelOrderCommand(order.getId()));
                log.info("주문 자동 취소 완료: {}", order.getOrderNumber());
            } catch (Exception e) {
                log.error("주문 자동 취소 실패: {}", order.getOrderNumber(), e);
            }
        }
    }

    // Redis TTL 만료 이벤트를 감지하여 호출되는 메서드 (예시)
    // 1. 비관적 락을 사용하여 결제와 동시에 일어나는 것을 방지 (정합성 보장)
    public void onOrderTimeout(Long orderId) {
        log.info("Redis 만료 이벤트 감지 - 주문 취소 시도: {}", orderId);
        try {
            cancelOrder(new CancelOrderCommand(orderId));
        } catch (IllegalStateException e) {
            // 결제가 이미 완료된 경우 등
            log.warn("Redis 만료 시점에 주문 취소 불가: {}", e.getMessage());
        }
    }
}