package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.settlement.SettlementCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@RequiredArgsConstructor
public class SettlementExecutionWriter implements ItemWriter<ExecutionResult> {

    private final SettlementHistoryRepository settlementHistoryRepository;
    private final SettlementQueueRepository settlementQueueRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends ExecutionResult> chunk) {
        for (ExecutionResult result : chunk) {
            SettlementHistory saved = settlementHistoryRepository.save(result.history());

            result.queueItems().forEach(q -> q.done(saved.getId()));
            settlementQueueRepository.saveAll(result.queueItems());

            eventPublisher.publish(new SettlementCreatedEvent(
                    saved.getId(),
                    saved.getSellerId(),
                    saved.getAmountSummary().settlementAmount()
            ));
        }
    }
}
