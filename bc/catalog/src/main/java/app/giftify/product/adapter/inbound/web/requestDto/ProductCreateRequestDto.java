package app.giftify.product.adapter.inbound.web.requestDto;

import app.giftify.product.domain.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.commons.lang3.StringUtils;

public record ProductCreateRequestDto(
        @NotBlank(message = "상품명 입력은 필수입니다.")
        String name,
        @NotBlank(message = "상품 설명 입력은 필수입니다.")
        String description,
        @NotNull(message = "가격 입력은 필수입니다.")
        @Positive(message = "가격은 0보다 커야 합니다.")
        Integer price,
        @NotNull(message = "재고 입력은 필수입니다.")
        @PositiveOrZero(message = "재고는 0이상이어야 합니다.")
        Integer stock,
        @NotNull(message = "카테고리 설정은 필수입니다.")
        ProductCategory category,
        String imageKey
) {
    public ProductCreateRequestDto {
        name = StringUtils.trimToNull(name);
        description = StringUtils.trimToNull(description);
    }
}