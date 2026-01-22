package app.giftify.order.adapter.in.event;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 외부 모듈(결제, 펀딩 등)에서 발생하는 이벤트를 수신하여 처리하는 리스너
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderUseCase orderUseCase;
    private final OrderRepositoryPort orderRepositoryPort;

    // 결제 성공 이벤트 수신
    // sourceType이 "ORDER"인 경우 해당 주문을 ORDERED 상태로 변경
    @EventListener
    @Transactional
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        if (!"ORDER".equals(event.getSourceType())) {
            return;
        }

        log.info("결제 성공 이벤트 수신 - 주문 처리 시작: paymentId={}, orderId={}", event.getPaymentId(), event.getUserId());
        
        try {
            // PaymentSucceededEvent의 userId가 주문 시 orderId로 전달된다고 가정
            Long orderId = event.getUserId(); 
            
            orderUseCase.payOrder(new OrderUseCase.PayOrderCommand(
                    orderId,
                    event.getPaymentId().toString() // paymentKey 대용으로 paymentId 사용
            ));
            log.info("주문 결제 처리 완료: orderId={}", orderId);
        } catch (Exception e) {
            log.error("주문 결제 처리 실패: paymentId={}", event.getPaymentId(), e);
        }
    }

    // 펀딩 취소 이벤트 수신
    // 해당 펀딩을 포함하는 모든 주문을 찾아 취소 처리
    @EventListener
    @Transactional
    public void handleFundingCanceled(FundingCanceledEvent event) {
        log.info("펀딩 취소 이벤트 수신 - 관련 주문 취소 시작: fundingId={}", event.getFundingId());
        cancelOrdersByFundingId(event.getFundingId());
    }

    // 펀딩 만료 이벤트 수신
    // 해당 펀딩을 포함하는 모든 주문을 찾아 취소 처리
    @EventListener
    @Transactional
    public void handleFundingExpired(FundingExpiredEvent event) {
        log.info("펀딩 만료 이벤트 수신 - 관련 주문 취소 시작: fundingId={}", event.getFundingId());
        cancelOrdersByFundingId(event.getFundingId());
    }

    // 특정 펀딩 ID와 연관된 모든 주문을 찾아 일괄 취소
    private void cancelOrdersByFundingId(Long fundingId) {
        List<Order> orders = orderRepositoryPort.findAllByFundingId(fundingId);
        for (Order order : orders) {
            try {
                orderUseCase.cancelOrder(new OrderUseCase.CancelOrderCommand(order.getId()));
                log.info("펀딩 이슈로 인한 주문 취소 완료: orderId={}, fundingId={}", order.getId(), fundingId);
            } catch (Exception e) {
                log.error("펀딩 이슈로 인한 주문 취소 실패: orderId={}, fundingId={}", order.getId(), fundingId, e);
            }
        }
    }
}
