
package app.giftify.settlement.application;

import app.giftify.settlement.adapter.outbound.batch.execution.ExecutionResult;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementAmountSummary;
import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.settlement.domain.model.SettlementQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SettlementExecutionService {
    private final SettlementQueueRepository queueRepository;

    public ExecutionResult execute(Long sellerId) {
        List<SettlementQueue> queues = queueRepository.findAllReadyQueuesBySellerId(sellerId);
        
        if (queues.isEmpty()) {
            return null;
        }

        queues.forEach(queue -> {
            queue.startProcessing();
            queue.getItem().processing();
        });

        SettlementAmountSummary summary = SettlementAmountSummary.of(queues);

        SettlementHistory history = new SettlementHistory(sellerId, summary, queues.size());
        
        return new ExecutionResult(history, queues);
    }
}