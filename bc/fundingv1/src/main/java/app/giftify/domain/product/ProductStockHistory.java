package app.giftify.domain.product;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PRODUCT_STOCK_HISTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long sellerId;

	@Column(nullable = false)
	private Long productId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StockChangeType changeType;

	@Column(nullable = false)
	private int delta;

	@Column(nullable = false)
	private int beforeStock;

	@Column(nullable = false)
	private int afterStock;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private ProductStockHistory(
		Long sellerId, Long productId, StockChangeType changeType,
		int delta, int beforeStock, int afterStock
	) {
		this.sellerId = sellerId;
		this.productId = productId;
		this.changeType = changeType;
		this.delta = delta;
		this.beforeStock = beforeStock;
		this.afterStock = afterStock;
		this.createdAt = LocalDateTime.now();
	}

	public static ProductStockHistory orderDeduct( // 주문 차감
		Long sellerId,
		Long productId,
		int quantity,
		int beforeStock,
		int afterStock
	) {
		return new ProductStockHistory(
			sellerId,
			productId,
			StockChangeType.ORDER_DEDUCT,
			-quantity,
			beforeStock,
			afterStock
		);
	}

	public static ProductStockHistory orderRestore( // 복원 재고 (펀딩
		Long sellerId,
		Long productId,
		int quantity,
		int beforeStock,
		int afterStock
	) {
		return new ProductStockHistory(
			sellerId,
			productId,
			StockChangeType.ORDER_RESTORE,
			quantity,
			beforeStock,
			afterStock
		);
	}

	public static ProductStockHistory manualAdjust( // 판매자 수동 조정
		Long sellerId,
		Long productId,
		int delta,
		int beforeStock,
		int afterStock
	) {
		return new ProductStockHistory(
			sellerId,
			productId,
			StockChangeType.MANUAL_ADJUST,
			delta,
			beforeStock,
			afterStock
		);
	}
}
