package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistoryJpa;
import app.giftify.product.application.port.in.StockHistorySearchCommand;
import org.springframework.data.domain.Page;

public interface ProductStockHistoryQueryRepository {
    Page<ProductStockHistoryJpa> searchStockHistories(Long sellerId, StockHistorySearchCommand searchCommand);
}
