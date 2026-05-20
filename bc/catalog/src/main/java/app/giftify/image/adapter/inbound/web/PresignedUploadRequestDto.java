package app.giftify.image.adapter.inbound.web;

import jakarta.validation.constraints.NotBlank;

public record PresignedUploadRequestDto(
	@NotBlank String domain,
	@NotBlank String contentType
) {
}
