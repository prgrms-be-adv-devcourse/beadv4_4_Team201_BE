package app.giftify.funding.application.service;

import app.giftify.funding.application.inbound.PaymentResultUseCase;
import app.giftify.funding.application.outbound.OrderPaymentPort;
import app.giftify.funding.application.outbound.OrderRepositoryPort;
import app.giftify.funding.domain.Order;
import app.giftify.funding.domain.exception.OrderErrorCode;
import app.giftify.funding.domain.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
