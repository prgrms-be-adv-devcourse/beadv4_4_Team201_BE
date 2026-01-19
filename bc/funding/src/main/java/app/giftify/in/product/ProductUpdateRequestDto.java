package app.giftify.in.product;

import static app.giftify.domain.product.ProductStatus.*;
import static app.giftify.domain.product.exception.ProductErrorCode.*;

import app.giftify.domain.product.ProductStatus;
import app.giftify.domain.product.exception.ProductException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequestDto(
	String name,
	String description,
	@Positive(message = "가격은 0보다 커야 합니다.")
	Integer price,
	@PositiveOrZero(message = "재고는 0이상이어야 합니다.")
	Integer stock,
	ProductStatus status
) {
	public ProductUpdateRequestDto {
		// DTO 입력 검증
		if (isEmpty())
			throw new ProductException(PRODUCT_UPDATE_EMPTY_REQUEST);
		if (status != null && status != ACTIVE && status != INACTIVE) {
			throw new ProductException(INVALID_PRODUCT_STATUS);
		}
		name = normalize(name);
		description = normalize(description);
	}

	private static String normalize(String value) { // 앞 뒤 공백 제거
		if (value == null)
			return null;
		return value.trim().isBlank() ? null : value.trim();
	}

	public boolean isEmpty() {
		return name == null
			&& description == null
			&& price == null
			&& stock == null
			&& status == null;
	}
}