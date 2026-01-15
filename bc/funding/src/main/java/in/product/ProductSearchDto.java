package in.product;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchDto {
	@NotNull
	private String keyword;
	private Integer minPrice;
	private Integer maxPrice;
	private Boolean inStock = false;
	private String sort = "latest";
	private int page = 0;
	private int size = 20;
}
