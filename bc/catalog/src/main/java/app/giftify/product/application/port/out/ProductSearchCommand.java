package app.giftify.product.application.port.out;

import lombok.Getter;
import lombok.Setter;

// Note: Using a class instead of a record to align with the DTO's getter/setter style.
@Getter
@Setter
public class ProductSearchCommand {
    private String keyword;
    private Integer minPrice;
    private Integer maxPrice;
    private Boolean inStock;
    private String sort;
    private int page;
    private int size;

    public ProductSearchCommand(String keyword, Integer minPrice, Integer maxPrice, Boolean inStock, String sort, int page, int size) {
        this.keyword = keyword;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.inStock = inStock != null && inStock;
        this.sort = sort != null ? sort : "latest";
        this.page = page;
        this.size = size;
    }
}
