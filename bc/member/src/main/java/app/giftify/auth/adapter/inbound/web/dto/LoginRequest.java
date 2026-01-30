package app.giftify.auth.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * POST /api/auth/login 요청 DTO.
 * Auth0 SPA SDK에서 발급받은 idToken을 전달합니다.
 */
public record LoginRequest(
        @NotBlank(message = "idToken은 필수입니다.")
        String idToken
) {
}
