package app.giftify.product.adapter.inbound.web.controller;

import app.giftify.product.adapter.inbound.web.requestDto.*;
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

    @Operation(summary = "상품 수정", description = """
            판매자가 자신의 상품을 수정합니다.
            
            
            **주의사항**:
            - SELLER 권한이 필요합니다.
            - 진행 중인 펀딩이 있을 경우, INACTIVE 상태로 변경이 불가합니다. (판매를 원치않으면 재고를 0으로 수정하세요)
            """)
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

    @Operation(summary = "상품 삭제", description = """
            판매자가 자신의 상품을 삭제합니다.
            
            
            **주의사항**:
            - SELLER 권한이 필요합니다.
            - INACTIVE 상태의 상품만 삭제가 가능합니다.
            - 상품 삭제는 Soft Delete로 처리됩니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "SELLER 권한 없음 또는 본인 상품이 아님"),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteProduct(
            @Parameter(description = "상품 ID", required = true, example = "1") @PathVariable("productId") Long productId,
            @Parameter(hidden = true) Long sellerId
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

    @Operation(summary = "나의 상품 조회", description = """
            판매자가 자신이 등록한 상품 목록을 조회합니다. SELLER 권한 필요.
            
            
            **필터링 옵션**:
            - `keyword` — 상품명 또는 설명 키워드 검색
            - `minPrice` / `maxPrice` — 가격 범위 필터
            - `inStock` — 재고 있는 상품만 조회 (기본값: false)
            - `status` — 상품 상태 필터 (DRAFT, ACTIVE, INACTIVE, REJECTED)
            - `deleted` — 삭제된 상품만 조회 (기본값: false). true 시 soft delete된 상품만 반환
            - `sort` — 정렬 기준 (latest, priceAsc, priceDesc / 기본값: latest)
            - `page` — 페이지 번호 (기본값: 0)
            - `size` — 페이지 크기 (기본값: 20, 최소: 1)
            """)
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

    @Operation(summary = "상품 검색 (ES)", description = """
            Elasticsearch 기반 상품 검색 API.
            키워드(name, description), 가격 범위, 카테고리 조건으로 ACTIVE 상품을 검색합니다.
            정렬 옵션: latest(최신순, 기본값), priceAsc(가격 낮은순), priceDesc(가격 높은순), relevance(ES 스코어순)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터 (size >= 1 필수)")
    })
    ResponseEntity<RsData<PageResponse<ProductDto>>> searchProductsByEs(
            @Valid @ModelAttribute ProductEsSearchDto searchDto
    );
}
