package app.giftify.funding.in.product;

import java.time.LocalDate;

import app.giftify.funding.domain.product.StockChangeType;
import lombok.Getter;
import lombok.Setter;

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
