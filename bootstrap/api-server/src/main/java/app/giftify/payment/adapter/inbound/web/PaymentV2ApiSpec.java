package app.giftify.payment.adapter.inbound.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentChargeResponse;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmRequest;
import app.giftify.payment.adapter.inbound.web.dto.PaymentConfirmResponse;
import app.giftify.support.common.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Payment V2", description = "결제 API (v2)")
public interface PaymentV2ApiSpec {

    @Operation(
            summary = "결제 생성 (예치금 충전)",
            description = """
                    예치금 충전을 위한 결제를 생성합니다.

                    **플로우**:
                    1. 이 API를 호출하여 Payment 레코드 생성
                    2. 응답으로 받은 orderId, amount를 사용하여 프론트엔드에서 Toss SDK 호출
                    3. Toss 결제 완료 후 /confirm API 호출

                    **참고**:
                    - orderId를 전달하지 않으면 자동 생성됩니다 (CHG-{UUID})
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "결제 생성 성공",
            content = @Content(schema = @Schema(implementation = PaymentChargeResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (금액이 0 이하 등)",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    ResponseEntity<RsData<PaymentChargeResponse>> charge(
            @Parameter(hidden = true) Long memberId,
            @RequestBody @Valid PaymentChargeRequest request
    );

    @Operation(
            summary = "결제 승인",
            description = """
                    Toss PG 결제 완료 후 서버 승인을 처리합니다.

                    **검증 항목**:
                    - 결제 소유자 확인 (memberId 일치 여부)
                    - 금액 조작 방지 (DB 저장 금액과 요청 금액 비교)

                    **승인 성공 시**:
                    - Payment 상태가 PAID로 변경
                    - PaymentPaidExternalEvent 발행 → 예치금 자동 충전

                    **승인 실패 시**:
                    - PG사 에러 코드/메시지 반환
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "승인 처리 완료 (성공/실패 여부는 응답 body의 success 필드 확인)",
            content = @Content(schema = @Schema(implementation = PaymentConfirmResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "금액 불일치",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증 토큰 누락 또는 유효하지 않음",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "결제 정보를 찾을 수 없음",
            content = @Content
    )
    ResponseEntity<RsData<PaymentConfirmResponse>> confirm(
            @Parameter(hidden = true) Long memberId,
            @RequestBody @Valid PaymentConfirmRequest request
    );
}
