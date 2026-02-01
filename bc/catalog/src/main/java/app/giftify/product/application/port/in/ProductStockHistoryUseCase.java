package app.giftify.product.application.port.in;

import app.giftify.product.adapter.inbound.web.requestDto.StockHistorySearchDto;
import app.giftify.product.adapter.inbound.web.responseDto.StockHistoryDto;
import app.giftify.shared.api.paging.PageResponse;

public interface ProductStockHistoryUseCase {
    PageResponse<StockHistoryDto> searchStockHistories(Long sellerId, StockHistorySearchDto searchDto);
}
