package app.giftify.product.adapter.inbound.web.requestDto;

import app.giftify.product.domain.StockChangeType;
import jakarta.validation.constraints.Min;
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

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    private int size = 20;
}
