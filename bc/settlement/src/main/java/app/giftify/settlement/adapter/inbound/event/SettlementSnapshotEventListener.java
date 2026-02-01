package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.SettlementItemService;
import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.domain.OrderItemSnapshot;
import app.giftify.settlement.domain.OrderSnapshot;
import app.giftify.settlement.domain.PaymentSnapshot;
import app.giftify.settlement.domain.exception.SettlementException;
import app.giftify.shared.domain.event.funding.FundingReceivedConfirmedEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.event.payment.PaymentCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSnapshotEventListener {
    private final OrderSnapshotRepository orderSnapshotRepository;
    private final OrderItemSnapshotRepository orderItemSnapshotRepository;
    private final PaymentSnapshotRepository paymentSnapshotRepository;
    private final SettlementItemService settlementItemService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFundingReceivedConfirmedEvent(FundingReceivedConfirmedEvent event) {
        InitializeSettlementItemCommand command = new InitializeSettlementItemCommand(
                event.fundingId(),
                event.confirmedAt()
        );

        try {
            settlementItemService.initializeSettlementItem(command);
        } catch (SettlementException e) {
            if (e.isRetryable()) {
                log.warn("[SettlementEventHandler] 재시도 대상 예외 발생, fundingId={}, message={}", event.fundingId(), e.getMessage(), e);
            } else {
                log.info("[SettlementEventHandler] 처리 불가 도메인/비즈니스 예외 발생, fundingId={}, message={}", event.fundingId(), e.getMessage());
            }
        } catch (Exception e) {
            log.error("[SettlementEventHandler] 예상치 못한 예외 발생, fundingId={}", event.fundingId(), e);
            throw e; // 필요시 재시도 가능하도록 상위로 던짐
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        orderSnapshotRepository.save(new OrderSnapshot(
            event.orderId(),
            event.orderNumber(),
            event.orderedAt()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderItemCreatedEvent(OrderItemCreatedEvent event) {
        orderItemSnapshotRepository.save(new OrderItemSnapshot(
            event.getOrderItemId(),
            event.getOrderId(),
            event.getFundingId(),
            event.getSellerId(),
            event.getPrice(),
            event.getAmount()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleteEvent(PaymentCompleteEvent event) {
        paymentSnapshotRepository.save(new PaymentSnapshot(
                event.paymentId(),
                event.orderNumber(),
                event.paymentKey(),
                event.transactionKey(),
                event.paidAt(),
                event.paidAmount(),
                event.method()
        ));
    }
}
