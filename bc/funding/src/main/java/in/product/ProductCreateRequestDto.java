package in.product;

public record ProductCreateRequestDto(
	String name,
	String description,
	int price,
	int stock
) {
}
