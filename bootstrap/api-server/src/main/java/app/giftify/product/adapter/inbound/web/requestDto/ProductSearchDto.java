package app.giftify.product.adapter.inbound.web.requestDto;

import jakarta.validation.constraints.Min;
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

    @Min(value = 0, message = "page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "size must be >= 1")
    private int size = 20;

    // todo dto->command
}
