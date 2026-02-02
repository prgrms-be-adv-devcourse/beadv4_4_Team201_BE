package app.giftify.product.adapter.inbound.web.requestDto;

import app.giftify.product.domain.StockChangeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StockHistorySearchDto {
    private Long productId; // optional: null이면 전체 상품
    private StockChangeType changeType; // optional: null이면 전체 타입
    private LocalDate fromDate; // optional
    private LocalDate toDate; // optional
    private int page = 0;
    private int size = 20;
}
