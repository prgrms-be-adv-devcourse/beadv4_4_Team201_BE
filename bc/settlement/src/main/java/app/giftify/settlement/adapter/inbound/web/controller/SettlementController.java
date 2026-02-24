package app.giftify.settlement.adapter.inbound.web.controller;

import app.giftify.security.common.CurrentMemberId;
import app.giftify.settlement.application.service.SettlementItemService;
import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementItemService settlementItemService;

    @GetMapping
    public ResponseEntity<RsData<PageResponse<SettlementSummary>>> getSettlementSummary(
            @CurrentMemberId Long sellerId,
            Pageable pageable
    ) {
        Page<SettlementSummary> page = settlementItemService.summarizeSettlements(sellerId, pageable);

        List<SettlementSummary> summaries = page.getContent();
        PageResponse<SettlementSummary> response = PageResponse.of(
                summaries, pageable.getPageNumber(), pageable.getPageSize(), page.getTotalElements());

        return ResponseEntity.ok(RsData.success(response));
    }
}
