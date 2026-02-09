package app.giftify.product.application.service;

import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.in.StockHistorySearchUseCase;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeResult;
import app.giftify.shared.api.exception.InfraException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStockHistoryService implements StockHistoryCreateUseCase, StockHistorySearchUseCase {

    private final ProductStockHistoryRepositoryPort stockHistoryRepositoryPort;

    // 재고 이력 생성
    @Transactional
    @Retryable(
            recover = "recoverCreateStockHistory", // @Recover 메서드가 여러 개일 때는 모호함 방지 필수(지금은 없어도 자동 매칭됨)
            value = {InfraException.class}, // 재시도 대상 예외
            maxAttempts = 3,                  // 최대 3회
            backoff = @Backoff(delay = 1000, multiplier = 2)// 1초 딜레이
    )
    @Override
    public void createStockHistory(StockChangeResult result) {
        ProductStockHistory history = ProductStockHistory.builder()
                .sellerId(result.sellerId())
                .productId(result.productId())
                .delta(result.delta())
                .beforeStock(result.beforeStock())
                .afterStock(result.afterStock())
                .changeType(result.changeType())
                .build();

        stockHistoryRepositoryPort.save(history);
    }

    @Recover
    public void recoverCreateStockHistory(InfraException e, StockChangeResult result) {
        log.error("[product] 재고 이력 저장 최종 실패 | productId: {}, delta: {}, changeType: {}",
                result.productId(), result.delta(), result.changeType(), e);
    }

    // 재고 이력 조회
    @Override
    public Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand) {
        return stockHistoryRepositoryPort.searchStockHistories(sellerId, searchCommand);
    }

    /**
     * TODO 관리자 메뉴
     * 관리자 재고 이력 조회
     * 관리자 재고 수정
     */
}
