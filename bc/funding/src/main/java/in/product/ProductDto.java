package in.product;

public record ProductDto(
	Long id,
	String sellerNickName,
	String name,
	String description,
	int price,
	int stock
) {
}
