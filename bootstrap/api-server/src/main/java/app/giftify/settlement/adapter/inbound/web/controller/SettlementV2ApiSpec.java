package app.giftify.settlement.adapter.inbound.web.controller;

import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.support.common.api.paging.PageResponse;
import app.giftify.support.common.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Settlement", description = "정산 API")
public interface SettlementV2ApiSpec {

    @Operation(
        summary = "정산 내역 요약 조회",
        description = "판매자의 월별 정산 내역을 페이지네이션으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음", content = @Content)
    ResponseEntity<RsData<PageResponse<SettlementSummary>>> getSettlementSummary(
        @Parameter(hidden = true) Long sellerId,
        Pageable pageable
    );
}
