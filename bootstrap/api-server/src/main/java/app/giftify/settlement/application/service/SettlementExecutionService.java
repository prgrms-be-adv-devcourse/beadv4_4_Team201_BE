
package app.giftify.settlement.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.settlement.adapter.outbound.batch.execution.ExecutionResult;
import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementAmountSummary;
import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.settlement.domain.event.SettlementCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementExecutionService {
	private static final Logger log = LoggerFactory.getLogger(SettlementExecutionService.class);


    private final SettlementHistoryRepository settlementHistoryRepository;
    private final SettlementQueueRepository settlementQueueRepository;
    private final EventPublisher eventPublisher;

    public ExecutionResult process(Long sellerId) {
        List<SettlementQueue> queues = settlementQueueRepository.findAllReadyQueuesBySellerId(sellerId);
        
        if (queues.isEmpty()) {
            return null;
        }

        log.info("판매자 별 집계를 수행합니다. sellerId = {}, count = {}", sellerId, queues.size());

        SettlementAmountSummary summary = SettlementAmountSummary.of(queues);

        SettlementHistory history = new SettlementHistory(sellerId, summary, queues.size());
        
        return new ExecutionResult(history, queues);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(ExecutionResult result) {
        result.queueItems().forEach(queue -> {
            queue.startProcessing();
            queue.getItem().processing();
        });

        SettlementHistory saved = settlementHistoryRepository.save(result.history());

        result.queueItems().forEach(q -> q.done(saved.getId()));
        settlementQueueRepository.saveAll(result.queueItems());

        eventPublisher.publish(new SettlementCreatedEvent(
                saved.getId(),
                saved.getSellerId(),
                saved.getAmountSummary().settlementAmount()
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(ExecutionResult result) {
        result.queueItems().forEach(queue -> {
            queue.fail();
            queue.getItem().failToExecute();
        });
        settlementQueueRepository.saveAll(result.queueItems());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsManual(ExecutionResult result) {
        result.queueItems().forEach(queue -> {
            queue.fail();
            queue.getItem().manual();
        });
        settlementQueueRepository.saveAll(result.queueItems());
    }
}