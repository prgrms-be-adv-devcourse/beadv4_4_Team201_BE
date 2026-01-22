package app.giftify.order.adapter.in.event;

import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.application.port.out.OrderRepositoryPort;
import app.giftify.order.domain.domain.Order;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

// 외부 모듈(결제, 펀딩 등)에서 발생하는 이벤트를 수신하여 처리하는 리스너
// TODO: 이벤트 안정성 및 순서 보장 전략
// * 1. Transactional Outbox Pattern 도입
// - 주문 상태 변경(DB)과 이벤트 발행(Message Queue)을 하나의 트랜잭션으로 묶음.
// - 별도의 'Outbox' 테이블에 이벤트를 저장하고, Relay 프로세스가 발행하여 유실 방지.
// * 2. 주문 상태 전이 유효성 체크 (State Machine)
// - 결제 성공 이벤트 수신 시, 현재 상태가 'CANCELED'이면 'ORDERED'로 변경 불가하게 차단.
// - 이벤트 순서가 꼬여 취소 후 성공이 오더라도 데이터 무결성 유지.
// * 3. 메시지 Key 설계 (Partitioning)
// - Kafka 사용 시, 'orderId'를 메시지 키로 설정하여 동일 주문 관련 이벤트는 
// 항상 동일한 파티션에 순서대로 쌓이도록 강제.
// * 4. 멱등성 컨슈머 구현
// - 모든 이벤트 핸들러는 처리 전 '이미 처리된 이벤트 ID'인지 체크.
// - DB 레벨에서 orderId + status 조합의 Unique 제약 조건 검토.
// * 5. 지연 메시지 처리 (Dead Letter Queue)
// - 주문 생성 전 결제 성공 이벤트가 먼저 도착한 경우, 즉시 실패 처리하지 않고
// 잠시 후 재시도(Retry Topic)하도록 설계.

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderUseCase orderUseCase;
    private final OrderRepositoryPort orderRepositoryPort;

    // 펀딩 성공(달성) 이벤트 수신 -> 주문 생성
    // Scenario 1: 펀딩 성공 시 주문 생성 및 이벤트 발행
    // Scenario 3: 멱등성 유지 (이미 주문이 생성된 경우 추가 생성 없이 종료)
    @EventListener
    @Transactional
    public void handleFundingAchieved(FundingAchievedEvent event) {
        log.info("펀딩 성공 이벤트 수신 - 주문 생성 시작: fundingId={}", event.getFundingId());

        // 멱등성 체크: 해당 펀딩 ID로 이미 생성된 주문이 있는지 확인
        List<Order> existingOrders = orderRepositoryPort.findAllByFundingId(event.getFundingId());
        if (!existingOrders.isEmpty()) {
            log.info("이미 해당 펀딩에 대한 주문이 존재합니다. 생성을 스킵합니다. fundingId={}", event.getFundingId());
            return;
        }

        try {
            // FundingAchievedEvent의 정보를 바탕으로 주문 생성 커맨드 구성
            // 펀딩 수령자(fundingReceiverId)를 주문 구매자(buyerId)로 가정 (비즈니스 요구사항에 따라 조정 필요)
            OrderUseCase.CreateOrderCommand command = new OrderUseCase.CreateOrderCommand(
                    event.getFundingReceiverId(),
                    Collections.singletonList(new OrderUseCase.CreateOrderCommand.OrderItemCommand(
                            event.getFundingId(),
                            event.getProductId(),
                            null, // sellerId는 이벤트에 없음, 필요 시 추가 정보 조회 필요
                            event.getFundingReceiverId(),
                            Money.zero(), // 가격 정보가 이벤트에 없음, 기본값 또는 조회 필요
                            Quantity.of(1) // 수량 정보 기본값
                    ))
            );

            orderUseCase.createOrder(command);
            log.info("펀딩 성공으로 인한 주문 생성 완료: fundingId={}", event.getFundingId());
        } catch (Exception e) {
            log.error("펀딩 성공으로 인한 주문 생성 실패: fundingId={}", event.getFundingId(), e);
        }
    }

    // 결제 성공 이벤트 수신
    // Scenario 4: 결제 성공 시 주문 확정 및 재고 차감 트리거
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

    // 결제 환불 이벤트 수신
    // Scenario 5: 결제 환불 시 주문 취소 및 재고 복구 트리거
    @EventListener
    @Transactional
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        if (!"ORDER".equals(event.getSourceType())) {
            return;
        }

        log.info("결제 환불 이벤트 수신 - 주문 취소 시작: paymentId={}, orderId={}", event.getPaymentId(), event.getUserId());

        try {
            Long orderId = event.getUserId();
            orderUseCase.cancelOrder(new OrderUseCase.CancelOrderCommand(orderId));
            log.info("환불로 인한 주문 취소 완료: orderId={}", orderId);
        } catch (Exception e) {
            log.error("환불로 인한 주문 취소 실패: paymentId={}", event.getPaymentId(), e);
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
