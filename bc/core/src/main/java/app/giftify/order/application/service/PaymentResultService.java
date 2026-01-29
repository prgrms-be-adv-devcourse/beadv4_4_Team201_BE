package app.giftify.order.application.service;

import org.springframework.stereotype.Service;

import app.giftify.order.application.inbound.PaymentResultUseCase;
import app.giftify.order.application.outbound.OrderRepositoryPort;
import app.giftify.order.domain.Order;
import app.giftify.order.domain.exception.OrderErrorCode;
import app.giftify.order.domain.exception.OrderException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentResultService implements PaymentResultUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public void completePayment(Long orderId, String paymentKey) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "찾을 수 없는 주문입니다. [ 주문 ID: " + orderId +" ]"));

        order.toOrdered(paymentKey);
        orderRepositoryPort.save(order);
    }

    @Override
    public void refundPayment(Long orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "찾을 수 없는 주문입니다. [ 주문 ID: " + orderId +" ]"));

        order.toRefunded();
        orderRepositoryPort.save(order);
    }

    @Override
    public void failPayment(Long orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "찾을 수 없는 주문입니다. [ 주문 ID: " + orderId +" ]"));

        order.toFailed();
        orderRepositoryPort.save(order);
    }

    @Override
    public void cancelPayment(Long orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND, "찾을 수 없는 주문입니다. [ 주문 ID: " + orderId +" ]"));

        order.toCancelled();
        orderRepositoryPort.save(order);
    }
}
