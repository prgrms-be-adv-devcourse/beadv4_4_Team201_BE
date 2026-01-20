package app.giftify.in.funding;

import app.giftify.app.funding.FundingFacade;
import app.giftify.shared.api.paging.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fundings")
public class FundingController {

    private final FundingFacade fundingFacade;

    @Operation( summary = "펀딩 단건 조회", description = "펀딩 ID로 단건 펀딩 정보를 조회합니다." )
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "펀딩 조회 성공"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FundingResponseDto> getFunding(@PathVariable Long id) {
        FundingResponseDto funding = fundingFacade.getFunding(id);
        return ResponseEntity.ok(funding);
    }

    @Operation( summary = "펀딩 목록 조회", description = "페이지 단위로 전체 펀딩 목록을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "펀딩 목록 조회 성공")
    })
    @GetMapping("list")
    public ResponseEntity<PageResponse<FundingResponseDto>> getFundings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<FundingResponseDto> fundings = fundingFacade.getFundings(page, size);
        return ResponseEntity.ok(fundings);
    }

    @Operation(summary = "펀딩 종료",description = "관리자가 펀딩을 종료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "펀딩 종료 성공"),
            @ApiResponse(responseCode = "400", description = "이미 종료된 펀딩 (F004)"),
            @ApiResponse(responseCode = "403",description = "관리자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)")
    })
    @PutMapping("/{id}/close")
    public ResponseEntity<FundingCompleteResponseDto> closeFunding(@PathVariable Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.closeFunding(id);
        return ResponseEntity.ok(funding);
    }

    @Operation( summary = "펀딩 만료 처리",description = "관리자가 펀딩을 만료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "펀딩 만료 처리 성공" ),
            @ApiResponse(responseCode = "400",description = "만료되지 않았거나 이미 종료된 펀딩 (F007, F004)"),
            @ApiResponse(responseCode = "403",description = "관리자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    @PutMapping("/{id}/expire")
    public ResponseEntity<FundingCompleteResponseDto> expireFunding(@PathVariable Long id) {
        // TODO: 관리자 권한 체크 필요
        FundingCompleteResponseDto funding = fundingFacade.expireFunding(id);
        return ResponseEntity.ok(funding);
    }

    @Operation(summary = "참여한 펀딩 단건 조회",description = "회원이 참여한 펀딩을 펀딩 ID로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "참여한 펀딩 조회 성공"),
            @ApiResponse(responseCode = "403",description = "본인이 참여하지 않은 펀딩 (F008)"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    @GetMapping("/participated/{id}")
    public ResponseEntity<MyFundingResponseDto> getParticipatedFunding(
            @PathVariable Long id,
            @Parameter(hidden = true) Long memberId
    ) {
        // TODO : 인증 적용
        MyFundingResponseDto funding = fundingFacade.getParticipatedFunding(id, memberId);
        return ResponseEntity.ok(funding);
    }

    @Operation(summary = "참여한 펀딩 목록 조회",description = "회원이 참여한 펀딩 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "참여한 펀딩 목록 조회 성공"),
            @ApiResponse(responseCode = "403",description = "회원 접근 권한 없음 (F008)")
    })
    @GetMapping("participated/list")
    public ResponseEntity<PageResponse<MyFundingResponseDto>> getParticipatedFundings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(hidden = true) Long memberId
            ){
        // TODO : 인증 적용
        PageResponse<MyFundingResponseDto> fundings = fundingFacade.getParticipatedFundings(page, size, memberId);
        return ResponseEntity.ok(fundings);
    }


// 펀딩 수락 -> 펀딩이 achieve 상태로 변경되면 수령자에게 알람 옴
// 펀딩 거절 -> 펀딩이 achieve 상태로 변경되면 수령자에게 알람 옴
// 나의 펀딩 (수령자) -> 진행중/완료로 나눠져야 함. 완료된 펀딩은 참여자 목록을 볼 수 있어야 함
// 펀딩 검색 -> 필요할까?

}