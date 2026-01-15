package app.giftify.in.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductCreateRequestDto(
	@NotBlank
	String name,
	@NotBlank
	String description,
	@NotNull
	@Positive
	Integer price,
	@NotNull
	Integer stock
) {
}
