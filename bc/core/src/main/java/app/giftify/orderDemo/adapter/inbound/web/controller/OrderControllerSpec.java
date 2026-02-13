package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.GetOrderDetailResponse;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.GetOrdersResponse;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order V2", description = "주문 API (v2)")
public interface OrderControllerSpec {
    @Operation(summary = "주문 생성", description = "로그인된 회원 ID로 단일/복수 주문 항목에 대해 주문을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "주문 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    @Parameter(
            name = "X-Idempotency-Key",
            description = "멱등성 검증을 위한 고유 키 (UUID 권장)",
            required = true,
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string")
    )
    ResponseEntity<RsData<PlaceOrderResult>> placeOrder(
            @Parameter(hidden = true) Long memberId,
            @RequestBody PlaceOrderRequest request,
            HttpServletRequest httpServletRequest
    );

    @Operation(summary = "주문 목록 조회", description = "로그인된 회원 ID로 주문 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<RsData<GetOrdersResponse>> getOrders(
            @Parameter(hidden = true) Long memberId,
            Pageable pageable
    );

    @Operation(summary = "주문 상세 조회", description = "로그인된 회원 ID와 주문 ID로 주문 상세를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<RsData<GetOrderDetailResponse>> getOrderDetail(
            @Parameter(hidden = true) Long memberId,
            Long orderId
    );
}
