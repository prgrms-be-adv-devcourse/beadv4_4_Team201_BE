package app.giftify.settlement.application.service.dto;

public record SettlementSummary(
        String settlementMonth,
        Long orderId,
        Long totalSalesAmount,
        Long totalSettlementAmount,
        Long itemCount,
        String status
) {
}
