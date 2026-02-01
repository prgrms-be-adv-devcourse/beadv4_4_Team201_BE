package app.giftify.member.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import app.giftify.member.adapter.in.web.dto.MemberResponse;
import app.giftify.member.adapter.in.web.dto.MemberUpdateRequest;
import app.giftify.member.adapter.in.web.dto.SignupRequest;
import app.giftify.security.common.context.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Member V2", description = "회원 정보 관리 API (v2)")
public interface MemberV2Api {
    @Operation(
            summary = "내 정보 조회",
            description = "인증된 사용자의 회원 정보를 조회합니다. authSub 필드는 응답에서 제외됩니다."
    )

    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = MemberResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "회원을 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<MemberResponse> getMe(
            @Parameter(hidden = true) @AuthenticatedMember String authSub
    );

    @Operation(
            summary = "내 정보 수정",
            description = """
                    회원 정보를 수정합니다. Partial Update를 지원하며, 전송된 필드만 수정됩니다.
                    
                    **주의사항**:
                    - 탈퇴한 회원(WITHDRAWN)은 수정할 수 없습니다 (403 반환)
                    - 닉네임 중복 확인은 별도 API를 사용하세요
                    """
    )

    @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(schema = @Schema(implementation = MemberResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "탈퇴한 회원은 정보를 수정할 수 없음",
            content = @Content
    )
    ResponseEntity<MemberResponse> updateMe(
            @Parameter(hidden = true) @AuthenticatedMember String authSub,
            @RequestBody @Valid MemberUpdateRequest request
    );

    @Operation(
            summary = "회원가입 (추가 정보 입력)",
            description = """
                    Auth0 로그인 후 추가 정보를 입력하여 회원가입을 완료합니다.
                    
                    **플로우**:
                    1. POST /api/v2/auth/login 호출 시 isNewUser: true 반환
                    2. 이 API로 추가 정보 입력 (모든 필드 선택사항)
                    3. 닉네임 미입력 시 User_XXXX 형식으로 자동 생성
                    
                    **주의사항**:
                    - 이미 가입된 사용자가 호출하면 409 Conflict 반환
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = MemberResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "이미 가입된 회원",
            content = @Content
    )
    ResponseEntity<MemberResponse> signup(
            @Parameter(hidden = true) @AuthenticatedMember String authSub,
            @RequestBody @Valid SignupRequest request
    );
}
