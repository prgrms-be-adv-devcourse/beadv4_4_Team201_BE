package app.giftify.product.application.service;

import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.in.StockHistorySearchUseCase;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
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
            retryFor = {TransientDataAccessException.class, RecoverableDataAccessException.class}, //재시도 대상 예외
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

    /**
     * 재고 이력 저장은 같은 JVM 내에서 DB에 INSERT하는 것 -> 네트워크 예외가 발생할 수 있는 지점은 JVM ↔ DB 구간
     * DataAccessException 하위 클래스 재시도하면 성공할 수 있는 예외
     * - TransientDataAccessException (쿼리 타임아웃, 락 충돌, 데드락, DB 일시적 과부하)
     * - RecoverableDataAccessException (DB 커넥션 장애, 네트워크 일시 장애)
     */
    @Recover
    public void recoverCreateStockHistory(DataAccessException e, StockChangeResult result) {
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
