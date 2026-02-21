package app.giftify.product.adapter.inbound.web.requestDto;

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
    private ProductCategory category;
    private ProductSearchSortType sort = ProductSearchSortType.RELEVANCE;

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    private int size = 20;
}
