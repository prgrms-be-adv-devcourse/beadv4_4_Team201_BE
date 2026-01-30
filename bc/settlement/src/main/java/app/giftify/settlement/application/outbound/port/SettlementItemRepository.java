package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.SettlementItem;

public interface SettlementItemRepository {

    SettlementItem save(SettlementItem settlementItem);
}
