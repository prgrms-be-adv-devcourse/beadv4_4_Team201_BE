package app.giftify.product.adapter.inbound.web.controller;

import app.giftify.product.adapter.inbound.web.requestDto.MyProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductCreateRequestDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductSearchDto;
import app.giftify.product.adapter.inbound.web.requestDto.ProductUpdateRequestDto;
import app.giftify.product.adapter.inbound.web.responseDto.MyProductDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;
import app.giftify.product.adapter.inbound.web.responseDto.StockHistoryDto;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.shared.api.paging.PageResponse;
import app.giftify.shared.api.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Product V2", description = "상품 관련 API")
public interface ProductV2ApiSpec {

    @Operation(summary = "상품 등록", description = "판매자가 새 상품을 등록합니다. SELLER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "상품 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "SELLER 권한 없음")
    })
    ResponseEntity<RsData<ProductDto>> createProduct(
            @Parameter(hidden = true) Long sellerId,
            @Valid @RequestBody ProductCreateRequestDto requestDto
    );

    @Operation(summary = "상품 승인/거절", description = "관리자가 상품 등록을 승인하거나 거절합니다. ADMIN 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 action (approve/reject만 허용)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ResponseEntity<RsData<String>> changeApproval(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable("id") Long id,
            @Parameter(description = "승인 액션", required = true, example = "approve",
                    schema = @Schema(allowableValues = {"approve", "reject"})) @PathVariable("action") String action
    );

    @Operation(summary = "상품 수정", description = "판매자가 자신의 상품 정보를 수정합니다. SELLER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "SELLER 권한 없음 또는 본인 상품이 아님"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ResponseEntity<RsData<ProductUpdateResponseDto>> updateProduct(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable("productId") Long productId,
            @Parameter(hidden = true) Long sellerId,
            @RequestBody ProductUpdateRequestDto requestDto
    );

    @Operation(summary = "상품 단건 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ResponseEntity<RsData<ProductDto>> getProduct(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable("id") Long id
    );

    @Operation(summary = "상품 검색", description = "키워드, 가격, 재고 등 조건으로 상품을 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터 (size >= 1 필수)")
    })
    ResponseEntity<RsData<PageResponse<ProductDto>>> searchProducts(
            @Valid @ModelAttribute ProductSearchDto searchDto
    );

    @Operation(summary = "나의 상품 조회", description = "판매자가 자신이 등록한 상품 목록을 조회합니다. SELLER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "SELLER 권한 없음")
    })
    ResponseEntity<RsData<PageResponse<MyProductDto>>> searchMyProducts(
            @Parameter(hidden = true) Long sellerId,
            @Valid @ModelAttribute MyProductSearchDto searchDto
    );

    @Operation(summary = "재고 이력 조회 (판매자)", description = "판매자가 자신의 상품 재고 변동 이력을 조회합니다. SELLER 권한 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "SELLER 권한 없음")
    })
    ResponseEntity<RsData<PageResponse<StockHistoryDto>>> searchStockHistories(
            @Parameter(hidden = true) Long sellerId,
            @Valid @ModelAttribute StockHistorySearchCommand searchCommand
    );

    @Operation(summary = "ES 상품 데이터 재동기화 (관리자)", description = """
            RDB의 승인된 상품(ACTIVE/INACTIVE)을 ES에 수동으로 전체 재동기화합니다. ADMIN 권한 필요.
            - ES 볼륨이 삭제됐었거나 ES 데이터가 꼬였을 때 수동 재동기화용으로 사용
            - ES 볼륨이 유지되는 한 서버(Spring Boot) 재시작 시 ES 데이터는 그대로 유지되므로 매번 호출할 필요 없음
            - 동기화 완료 시 처리된 상품 건수를 반환
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음")
    })
    ResponseEntity<RsData<String>> syncProductsToEs();
}
