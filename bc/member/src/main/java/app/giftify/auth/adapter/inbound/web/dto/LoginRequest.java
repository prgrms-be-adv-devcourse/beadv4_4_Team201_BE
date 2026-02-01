package app.giftify.auth.adapter.inbound.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
	@Schema(
		description = "Auth0 SPA SDK에서 발급받은 ID Token",
		example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
	)
	@NotBlank(message = "idToken은 필수입니다.")
	String idToken
) {
}
