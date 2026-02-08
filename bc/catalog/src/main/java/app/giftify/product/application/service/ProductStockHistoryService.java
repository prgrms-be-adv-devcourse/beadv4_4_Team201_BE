package app.giftify.product.application.service;

import app.giftify.product.application.port.in.ProductStockHistoryUseCase;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.domain.ProductStockHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductStockHistoryService implements ProductStockHistoryUseCase {

    private final ProductStockHistoryRepositoryPort stockHistoryRepositoryPort;

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
