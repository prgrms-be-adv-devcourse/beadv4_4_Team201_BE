package app.giftify.product.application.port.out;

import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductSearchSortType;

public record ProductEsSearchCommand(
        String keyword,
        Integer minPrice,
        Integer maxPrice,
        ProductCategory category,
        ProductSearchSortType sort,
        int page,
        int size
) {
}
