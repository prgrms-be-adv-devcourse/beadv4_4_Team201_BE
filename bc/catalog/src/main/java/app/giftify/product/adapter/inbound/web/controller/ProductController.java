package app.giftify.product.adapter.inbound.web.controller;

import app.giftify.product.adapter.inbound.web.requestDto.*;
import app.giftify.product.adapter.inbound.web.responseDto.ProductDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductSnapshotDto;
import app.giftify.product.adapter.inbound.web.responseDto.ProductUpdateResponseDto;
import app.giftify.product.adapter.inbound.web.responseDto.StockHistoryDto;
import app.giftify.product.application.port.in.*;
import app.giftify.product.domain.exception.ProductException;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.paging.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static app.giftify.product.domain.exception.ProductErrorCode.INVALID_APPROVAL_ACTION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Slf4j
public class ProductController {
    private final ProductCreateUseCase productCreateUseCase;
    private final ProductGetUseCase productGetUseCase;
    private final ProductSearchUseCase productSearchUseCase;
    private final ProductApproveUseCase productApproveUseCase;
    private final ProductRejectUseCase productRejectUseCase;
    private final ProductUpdateUseCase productUpdateUseCase;
    private final ProductStockHistoryUseCase productStockHistoryUseCase;
    private final ProductSnapshotCreateUseCase productSnapshotCreateUseCase;

    // 상품 등록
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductDto> createProduct(
            @CurrentMemberId Long sellerId,
            @Valid @RequestBody ProductCreateRequestDto requestDto
    ) {
        var command = new ProductCreateCommand(
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.stock()
        );
        ProductResult result = productCreateUseCase.createProduct(sellerId, command);
        ProductDto responseDto = ProductDto.from(result);
        return ResponseEntity.status(CREATED).body(responseDto);
    }

    // 상품 등록 승인 및 거절 (관리자)
    @PatchMapping("/{id}/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeApproval(
            @PathVariable("id") Long id,
            @PathVariable("action") String action
    ) {
        switch (action) {
            case "approve" -> {
                productApproveUseCase.approveProduct(id);
                return ResponseEntity.status(OK).body("상품 등록 승인, 상품 ID: " + id);
            }
            case "reject" -> {
                productRejectUseCase.rejectProduct(id);
                return ResponseEntity.status(OK).body("상품 등록 거절, 상품 ID: " + id);
            }
            default -> throw new ProductException(INVALID_APPROVAL_ACTION);
        }
    }

    // 상품 수정
    @PreAuthorize("hasRole('SELLER')")
    @PatchMapping("/my/{productId}")
    public ResponseEntity<ProductUpdateResponseDto> updateProduct(
            @PathVariable("productId") Long productId,
            @CurrentMemberId Long sellerId,
            @RequestBody ProductUpdateRequestDto requestDto
    ) {
        ProductUpdateResult result = productUpdateUseCase.updateProduct(productId, sellerId, requestDto);
        ProductUpdateResponseDto responseDto = ProductUpdateResponseDto.from(result);
        return ResponseEntity.status(OK).body(responseDto);
    }

    // 상품 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(
            @PathVariable("id") Long id
    ) {
        ProductResult result = productGetUseCase.getProduct(id);
        ProductDto responseDto = ProductDto.from(result);
        return ResponseEntity.status(OK).body(responseDto);
    }

    // 상품 검색
    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductDto>> searchProducts(
            @ModelAttribute ProductSearchDto searchDto
    ) {
        PageResponse<ProductResult> resultPage = productSearchUseCase.searchProducts(searchDto);
        List<ProductDto> dtoList = resultPage.content().stream()
                .map(ProductDto::from)
                .collect(Collectors.toList());
        var response = PageResponse.of(dtoList, resultPage.pageNumber(), resultPage.pageSize(), resultPage.totalElements());
        return ResponseEntity.status(OK).body(response);
    }

    // (판매자) 나의 상품 조회
    @GetMapping("/my")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<PageResponse<ProductDto>> searchMyProducts(
            @CurrentMemberId Long sellerId,
            @ModelAttribute MyProductSearchDto searchDto
    ) {
        PageResponse<ProductResult> resultPage = productSearchUseCase.searchMyProducts(sellerId, searchDto);
        List<ProductDto> dtoList = resultPage.content().stream()
                .map(ProductDto::from)
                .collect(Collectors.toList());
        var response = PageResponse.of(dtoList, resultPage.pageNumber(), resultPage.pageSize(), resultPage.totalElements());
        return ResponseEntity.status(OK).body(response);
    }

    // (판매자) 재고 이력 조회
    @GetMapping("/my/stock-histories")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<PageResponse<StockHistoryDto>> searchStockHistories(
            @CurrentMemberId Long sellerId,
            @ModelAttribute StockHistorySearchDto searchDto
    ) {
        PageResponse<StockHistoryDto> stockHistories = productStockHistoryUseCase.searchStockHistories(sellerId, searchDto);
        return ResponseEntity.status(OK).body(stockHistories);
    }

    /**
     * 상품 스냅샷 생성 (내부 서비스 호출용)
     * - productId List를 받아 스냅샷 생성 후 반환
     * - 요청 순서대로 스냅샷 반환 보장
     */
    @PostMapping("/snapshots")
    public ResponseEntity<List<ProductSnapshotDto>> createProductSnapshots(
            @RequestBody ProductSnapshotRequestDto requestDto
    ) {
        List<ProductSnapshotDto> snapshotDtos = productSnapshotCreateUseCase.createProductSnapshots(requestDto);
        return ResponseEntity.status(CREATED).body(snapshotDtos);
    }
}
