package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.jpa.repository.SettlementItemRepository;
import app.giftify.settlement.application.inbound.InitializeSettlementItemCommand;
import app.giftify.settlement.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementItemService {

    private final SettlementItemRepository settlementItemRepository;

    private final OrderSnapshotService orderSnapshotService;
    private final OrderItemSnapshotService orderItemSnapshotService;
    private final PaymentSnapshotService paymentSnapshotService;
    private final FeePolicyService feePolicyService;

    public void initializeSettlementItem(InitializeSettlementItemCommand command) {
        SettlementSource source = fetchSettlementSource(command);

        SettlementCore core = SettlementCalculator.calculate(
                source.getPaidAmount(),
                feePolicyService.getPlatformFeeRate(),
                feePolicyService.getPgFeeRate()
        );

        SettlementItem paymentItem = SettlementItem.createPaymentItem(
                source,
                core,
                command.confirmedAt()
        );

        settlementItemRepository.save(paymentItem);
    }

    private SettlementSource fetchSettlementSource(InitializeSettlementItemCommand command) {
        OrderItemSnapshot item = orderItemSnapshotService.findByIdFundingId(command.fundingId());
        OrderSnapshot order = orderSnapshotService.findById(item.getOrderId());
        PaymentSnapshot payment = paymentSnapshotService.findByOrderNumber(order.getOrderNumber());
        return new SettlementSource(item, order, payment);
    }
}
