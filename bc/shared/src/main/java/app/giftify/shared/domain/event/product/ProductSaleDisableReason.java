package app.giftify.shared.domain.event.product;

public enum ProductSaleDisableReason {
	SOLD_OUT("재고 소진된 상품"),
	STOPPED_BY_SELLER("판매 중지된 상품");

	private final String reason;

	ProductSaleDisableReason(String reason) {
		this.reason = reason;
	}
}
