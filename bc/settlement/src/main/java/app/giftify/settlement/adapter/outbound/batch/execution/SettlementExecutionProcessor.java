package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.application.service.SettlementExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;

@RequiredArgsConstructor
public class SettlementExecutionProcessor implements ItemProcessor<Long, ExecutionResult> {

    private final SettlementExecutionService settlementExecutionService;

    @Override
    public ExecutionResult process(Long sellerId) {
        return settlementExecutionService.process(sellerId);
    }
}
