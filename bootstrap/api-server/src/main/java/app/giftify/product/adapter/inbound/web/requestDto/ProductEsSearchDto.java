package app.giftify.product.adapter.inbound.web.requestDto;

import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductSearchSortType;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductEsSearchDto {
    private String keyword;
    private Integer minPrice;
    private Integer maxPrice;
    private String category;
    private ProductSearchSortType sort = ProductSearchSortType.RELEVANCE;

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    private int size = 20;

    public static ProductEsSearchCommand toCommand(ProductEsSearchDto dto) {
        ProductCategory category = null;
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            category = ProductCategory.valueOf(dto.getCategory().toUpperCase());
        }
        return new ProductEsSearchCommand(
                dto.getKeyword(),
                dto.getMinPrice(),
                dto.getMaxPrice(),
                category,
                dto.getSort(),
                dto.getPage(),
                dto.getSize()
        );
    }
}
