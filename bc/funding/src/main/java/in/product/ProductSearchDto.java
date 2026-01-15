package in.product;

public record ProductSearchDto(
	String keyword,
	Integer minPrice,
	Integer maxPrice,
	Boolean inStock,
	String sort,
	int page,
	int size
) {
}
