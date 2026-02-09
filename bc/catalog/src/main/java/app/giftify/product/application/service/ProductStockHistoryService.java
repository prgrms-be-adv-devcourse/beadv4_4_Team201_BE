package app.giftify.product.application.service;

import app.giftify.product.application.port.in.StockHistoryCreateUseCase;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.in.StockHistorySearchUseCase;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import app.giftify.product.domain.StockChangeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStockHistoryService implements StockHistoryCreateUseCase, StockHistorySearchUseCase {

    private final ProductStockHistoryRepositoryPort stockHistoryRepositoryPort;

    // 재고 이력 생성
    @Transactional
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
