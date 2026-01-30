package app.giftify.settlement.adapter.inbound.event;

import app.giftify.settlement.application.OrderItemSnapshotService;
import app.giftify.settlement.application.OrderSnapshotService;
import app.giftify.settlement.application.PaymentSnapshotService;
import app.giftify.settlement.application.SettlementItemService;
import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.domain.OrderItemSnapshot;
import app.giftify.settlement.domain.OrderSnapshot;
import app.giftify.settlement.domain.PaymentSnapshot;
import app.giftify.shared.domain.event.funding.FundingReceivedConfirmedEvent;
import app.giftify.shared.domain.event.order.OrderCreatedEvent;
import app.giftify.shared.domain.event.order.OrderItemCreatedEvent;
import app.giftify.shared.domain.event.payment.PaymentCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSnapshotEventListener {
    private final OrderSnapshotService orderSnapshotService;
    private final OrderItemSnapshotService orderItemSnapshotService;
    private final PaymentSnapshotService paymentSnapshotService;
    private final SettlementItemService settlementItemService;

    @EventListener
    public void handleFundingReceivedConfirmedEvent(FundingReceivedConfirmedEvent event) {
        InitializeSettlementItemCommand command = new InitializeSettlementItemCommand(
                event.fundingId(),
                event.confirmedAt()
        );

        settlementItemService.initializeSettlementItem(command);
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        orderSnapshotService.save(new OrderSnapshot(
            event.orderId(),
            event.orderNumber(),
            event.orderedAt()
        ));
    }

    @EventListener
    public void handleOrderItemCreatedEvent(OrderItemCreatedEvent event) {
        orderItemSnapshotService.save(new OrderItemSnapshot(
            event.orderItemId(),
            event.orderId(),
            event.fundingId(),
            event.sellerId(),
            event.quantity(),
            event.unitPrice(),
            event.totalAmount()
        ));
    }

    @EventListener
    public void handlePaymentCompleteEvent(PaymentCompleteEvent event) {
        paymentSnapshotService.save(new PaymentSnapshot(
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
