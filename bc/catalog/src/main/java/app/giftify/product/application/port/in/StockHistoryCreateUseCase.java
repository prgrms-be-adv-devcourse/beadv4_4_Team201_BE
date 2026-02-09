package app.giftify.product.application.port.in;

import app.giftify.product.domain.StockChangeResult;

public interface StockHistoryCreateUseCase {
    void createStockHistory(StockChangeResult result);
}
