package app.giftify.product.application.port.out;

import app.giftify.product.domain.ProductStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyProductSearchCommand extends ProductSearchCommand {
    private ProductStatus status;
    private boolean deleted;

    public MyProductSearchCommand(String keyword, Integer minPrice, Integer maxPrice, Boolean inStock, String sort, int page, int size, ProductStatus status, boolean deleted) {
        super(keyword, minPrice, maxPrice, inStock, sort, page, size);
        this.status = status;
        this.deleted = deleted;
    }
}
