package app.giftify.product.application.port.in;

import app.giftify.product.domain.StockChangeType;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record StockHistorySearchCommand(
        Long productId,
        StockChangeType changeType,
        LocalDate fromDate,
        LocalDate toDate,
        String sort,
        @Min(value = 0, message = "page must be >= 0")
        Integer page,
        @Min(value = 1, message = "size must be >= 1")
        Integer size
) {
    public StockHistorySearchCommand {
        if (page == null)
            page = 0;
        if (size == null)
            size = 20;
        if (sort == null || sort.isBlank())
            sort = "desc";
    }
}
