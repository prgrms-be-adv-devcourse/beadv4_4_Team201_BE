package app.giftify.product.adapter.inbound.web.requestDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchDto {
    private String keyword;
    private Integer minPrice;
    private Integer maxPrice;
    private Boolean inStock = false;
    private String sort = "latest";
    private int page = 0;
    private int size = 20;
}
