package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementAmountSummary;
import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.settlement.domain.status.SettlementQueueStatus;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SettlementExecutionProcessor implements ItemProcessor<Long, ExecutionResult> {

    private final SettlementQueueRepository settlementQueueRepository;

    @Override
    public ExecutionResult process(Long sellerId) {
        List<SettlementQueue> queues = settlementQueueRepository.findAllBySellerIdAndStatus(
                sellerId, SettlementQueueStatus.READY
        );

        if (queues.isEmpty()) {
            return null;
        }

        Money totalSales = Money.zero();
        Money totalPlatformFee = Money.zero();
        Money totalPgFee = Money.zero();
        Money totalSettlement = Money.zero();

        LocalDateTime now = LocalDateTime.now();

        for (SettlementQueue queue : queues) {
            SettlementItem item = queue.getItem();

            totalSales = totalSales.plus(item.getCore().paidAmount());
            totalPlatformFee = totalPlatformFee.plus(item.getCore().platformFee());
            totalPgFee = totalPgFee.plus(item.getCore().pgFee());
            totalSettlement = totalSettlement.plus(item.getCore().settlementAmount());
        }

        SettlementAmountSummary summary = new SettlementAmountSummary(
                totalSales, totalPlatformFee, totalPgFee, totalSettlement
        );

        SettlementHistory history = new SettlementHistory(sellerId, summary, queues.size());

        return new ExecutionResult(history, queues);
    }
}