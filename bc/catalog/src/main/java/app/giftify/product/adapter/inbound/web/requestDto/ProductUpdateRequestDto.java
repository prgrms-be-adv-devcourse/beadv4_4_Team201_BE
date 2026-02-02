package app.giftify.product.adapter.inbound.web.requestDto;

import app.giftify.product.domain.ProductStatus;
import app.giftify.product.domain.exception.ProductException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import static app.giftify.product.domain.ProductStatus.ACTIVE;
import static app.giftify.product.domain.ProductStatus.INACTIVE;
import static app.giftify.product.domain.exception.ProductErrorCode.INVALID_PRODUCT_STATUS;
import static app.giftify.product.domain.exception.ProductErrorCode.PRODUCT_UPDATE_EMPTY_REQUEST;

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
        name = normalize(name);
        description = normalize(description);

        if (name == null && description == null && price == null && stock == null && status == null) {
            throw new ProductException(PRODUCT_UPDATE_EMPTY_REQUEST);
        }
        if (status != null && status != ACTIVE && status != INACTIVE) {
            throw new ProductException(INVALID_PRODUCT_STATUS);
        }
    }

    private static String normalize(String value) { // 앞 뒤 공백 제거
        if (value == null)
            return null;
        return value.trim().isBlank() ? null : value.trim();
    }
}
