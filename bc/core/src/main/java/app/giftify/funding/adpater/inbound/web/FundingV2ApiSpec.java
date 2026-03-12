package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.adpater.inbound.dto.*;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import app.giftify.shared.domain.type.FundingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    ResponseEntity<RsData<FundingResponseDto>> getFunding(@PathVariable("id") Long id);

    @Operation(summary = "펀딩 목록 조회", description = "페이지 단위로 전체 펀딩 목록을 조회합니다.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "펀딩 목록 조회 성공")
    })
    ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @Operation(summary = "펀딩 종료", description = "관리자가 펀딩을 종료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "펀딩 종료 성공"),
            @ApiResponse(responseCode = "400", description = "이미 종료된 펀딩 (F004)"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<FundingCompleteResponseDto>> closeFunding(@PathVariable("id") Long id);

    @Operation(summary = "펀딩 만료 처리", description = "관리자가 펀딩을 만료 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "펀딩 만료 처리 성공"),
            @ApiResponse(responseCode = "400", description = "만료되지 않았거나 이미 종료된 펀딩 (F007, F004)"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<FundingCompleteResponseDto>> expireFunding(@PathVariable("id") Long id);

    @Operation(summary = "참여한 펀딩 단건 조회", description = "회원이 참여한 펀딩을 펀딩 ID로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여한 펀딩 조회 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 참여하지 않은 펀딩 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<ContributeFundingResponseDto>> getParticipatedFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(summary = "참여한 펀딩 목록 조회", description = "회원이 참여한 펀딩 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참여한 펀딩 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "회원 접근 권한 없음 (F008)")
    })
    ResponseEntity<RsData<PageResponse<ContributeFundingResponseDto>>> getParticipatedFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(summary = "펀딩 거절",description = "수령자가 펀딩을 거절 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "펀딩 거절 처리 성공"),
            @ApiResponse(responseCode = "400",description = "진행 중이 아닌 펀딩 (F003)"),
            @ApiResponse(responseCode = "403",description = "수령자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<FundingCompleteResponseDto>> refuseFunding(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(summary = "펀딩 수락",description = "수령자가 펀딩을 수락 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "펀딩 수락 처리 성공"),
            @ApiResponse(responseCode = "400",description = "진행 중이 아닌 펀딩 (F003)"),
            @ApiResponse(responseCode = "403",description = "수령자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<FundingCompleteResponseDto>> requestFundingAcceptance(
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(summary = "나의 펀딩 단건 조회",description = "수령자가 본인의 단건 펀딩 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",description = "펀딩 조회 성공"),
            @ApiResponse(responseCode = "403",description = "수령자 권한 없음 (F008)"),
            @ApiResponse(responseCode = "404",description = "펀딩을 찾을 수 없음 (F006)")
    })
    ResponseEntity<RsData<MyFundingResponseDto>> getMyFunding(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(summary = "나의 펀딩 목록 조회",description = "수령자가 본인의 펀딩 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나의 펀딩 리스트 조회 성공"),
            @ApiResponse(responseCode = "403", description = "수령자 권한 없음 (F008)")
    })
    ResponseEntity<RsData<PageResponse<MyFundingSummaryDto>>> getMyFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) FundingStatus status,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "친구의 진행 중인 펀딩 목록 조회",
            description = "친구로 등록된 사용자의 진행 중(IN_PROGRESS) 펀딩 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구 펀딩 목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "친구 관계 아님 (F008)"),
            @ApiResponse(responseCode = "404", description = "수령자를 찾을 수 없음 (F012)")
    })
    ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFriendFundings(
            @PathVariable("friendId") Long friendId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "친구의 진행 중인 단건 펀딩 조회",
            description = "친구로 등록된 사용자의 진행 중(IN_PROGRESS)인 단건 펀딩을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구 펀딩 단건 조회 성공"),
            @ApiResponse(responseCode = "403", description = "친구 관계 아님 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)"),
            @ApiResponse(responseCode = "404", description = "수령자를 찾을 수 없음 (F012)")
    })
    ResponseEntity<RsData<FundingResponseDto>> getFriendFunding(
            @PathVariable("friendId") Long friendId,
            @PathVariable("id") Long id,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);

    @Operation(
            summary = "내 친구들의 진행 중인 펀딩 리스트 조회",
            description = "친구로 등록된 사용자들의 진행 중(IN_PROGRESS)인 펀딩 리스트를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "친구들 펀딩 리스트 조회 성공"),
            @ApiResponse(responseCode = "403", description = "친구 관계 아님 (F008)"),
            @ApiResponse(responseCode = "404", description = "펀딩을 찾을 수 없음 (F006)"),
            @ApiResponse(responseCode = "404", description = "수령자를 찾을 수 없음 (F012)")
    })
    ResponseEntity<RsData<PageResponse<FundingResponseDto>>> getFriendsFundings(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @CurrentMemberId Long memberId);
}
