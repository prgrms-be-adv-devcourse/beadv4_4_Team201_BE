package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Funding V2", description = "펀딩 관련 API")
public interface FundingV2ApiSpec {

    @Operation(summary = "펀딩 단건 조회", description = "펀딩 ID로 단건 펀딩 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "펀딩 조회 성공"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<FundingResponseDto>> getFunding(@PathVariable Long id);

    @Operation(summary = "펀딩 목록 조회", description = "페이지 단위로 전체 펀딩 목록을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "펀딩 목록 조회 성공")
    })
    ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFundings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
