package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.settlement.SettlementCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RequiredArgsConstructor
public class SettlementExecutionWriter implements ItemWriter<ExecutionResult> {

    private final SettlementHistoryRepository settlementHistoryRepository;
    private final SettlementQueueRepository settlementQueueRepository;
    private final EventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends ExecutionResult> chunk) {
        for (ExecutionResult result : chunk) {
            try {
                SettlementHistory saved = settlementHistoryRepository.save(result.history());

                result.queueItems().forEach(q -> q.done(saved.getId()));
                settlementQueueRepository.saveAll(result.queueItems());

                eventPublisher.publish(new SettlementCreatedEvent(
                        saved.getId(),
                        saved.getSellerId(),
                        saved.getAmountSummary().settlementAmount()
                ));
            } catch (DataIntegrityViolationException e) {
                log.warn("중복 정산 감지 - sellerId: {}, settlementDate: {}. skip 처리합니다.",
                        result.history().getSellerId(), result.history().getSettlementDate(), e);
            }
        }
    }
}
