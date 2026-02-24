package app.giftify.settlement.application.service.dto;

import app.giftify.settlement.domain.status.SettlementStatus;

import java.util.List;

public record SettlementSummary(
        String settlementMonth,
        Long orderId,
        Long totalSalesAmount,
        Long totalSettlementAmount,
        SettlementStatus status,
        List<Long> settlementItemIds
) {
}
