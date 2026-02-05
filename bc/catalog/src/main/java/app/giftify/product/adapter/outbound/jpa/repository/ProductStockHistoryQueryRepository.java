package app.giftify.product.adapter.outbound.jpa.repository;

import app.giftify.product.adapter.inbound.web.requestDto.StockHistorySearchDto;
import app.giftify.product.adapter.outbound.jpa.entity.ProductStockHistory;
import org.springframework.data.domain.Page;

public interface ProductStockHistoryQueryRepository {
    Page<ProductStockHistory> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto);
}
