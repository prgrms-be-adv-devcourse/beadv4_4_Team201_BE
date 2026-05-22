package app.giftify.settlement.adapter.outbound.batch.execution;

import app.giftify.settlement.domain.model.SettlementHistory;
import app.giftify.settlement.domain.model.SettlementQueue;

import java.util.List;

public record ExecutionResult(
        SettlementHistory history,
        List<SettlementQueue> queueItems
) {
}