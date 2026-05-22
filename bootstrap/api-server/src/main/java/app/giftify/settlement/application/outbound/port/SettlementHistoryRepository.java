package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.model.SettlementHistory;

public interface SettlementHistoryRepository {

    SettlementHistory save(SettlementHistory history);
}