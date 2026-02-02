package app.giftify.wallet.adapter.inbound.web;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import app.giftify.shared.api.response.CommonResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WalletBalanceResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WalletHistoryResponse;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletRequest;
import app.giftify.wallet.adapter.inbound.web.dto.WithdrawWalletResponse;
import app.giftify.wallet.domain.TransactionType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Wallet V2", description = "예치금 지갑 API (v2)")
public interface WalletV2Api {

    @Operation(
            summary = "예치금 출금 요청",
            description = """
                    예치금을 지정한 은행 계좌로 출금 요청합니다.

                    **주의사항**:
                    - 출금 가능 금액은 현재 잔액 이하여야 합니다
                    - 출금 처리는 영업일 기준 1-2일 소요될 수 있습니다
                    - 출금 수수료가 부과될 수 있습니다

                    **은행 코드 예시**:
                    - 004: KB국민은행
                    - 088: 신한은행
                    - 020: 우리은행
                    - 081: 하나은행
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "출금 요청 성공",
            content = @Content(schema = @Schema(implementation = WithdrawWalletResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (금액 부족, 잘못된 계좌 정보 등)",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "지갑을 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<CommonResponse<WithdrawWalletResponse>> withdraw(
            @Parameter(hidden = true) Long memberId,
            @RequestBody @Valid WithdrawWalletRequest request
    );

    @Operation(
            summary = "예치금 잔액 조회",
            description = "현재 로그인한 사용자의 예치금 잔액을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = WalletBalanceResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "지갑을 찾을 수 없음 (회원가입 후 첫 조회 시 자동 생성)",
            content = @Content
    )
    ResponseEntity<CommonResponse<WalletBalanceResponse>> getBalance(
            @Parameter(hidden = true) Long memberId
    );


    @Operation(
            summary = "예치금 거래 내역 조회",
            description = """
                    현재 로그인한 사용자의 예치금 거래 내역을 조회합니다.
                    
                    **거래 유형**:
                    - CHARGE: 충전
                    - WITHDRAW: 출금
                    - PAYMENT: 결제
                    
                    **페이징**:
                    - page: 페이지 번호 (0부터 시작, 기본값: 0)
                    - size: 페이지 크기 (기본값: 20, 최대: 100)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = WalletHistoryResponse.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    ResponseEntity<CommonResponse<Page<WalletHistoryResponse>>> getHistory(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "거래 유형 필터 (선택)", example = "CHARGE")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    );
}