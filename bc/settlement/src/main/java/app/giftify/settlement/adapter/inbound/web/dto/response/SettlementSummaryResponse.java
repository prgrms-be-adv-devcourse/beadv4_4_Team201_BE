package app.giftify.settlement.adapter.inbound.web.dto.response;

import app.giftify.settlement.application.service.dto.SettlementSummary;

import java.util.List;

public record SettlementSummaryResponse(
        List<SettlementSummary> summaries,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}