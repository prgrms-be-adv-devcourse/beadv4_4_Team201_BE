package app.giftify.auth.adapter.inbound.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.auth.adapter.inbound.web.dto.LoginRequest;
import app.giftify.auth.adapter.inbound.web.dto.LoginResponse;
import app.giftify.auth.application.AuthService;
import app.giftify.auth.application.inbound.LoginUseCase;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController implements AuthV2ApiSpec {

	private final AuthService authService;
	private final LoginUseCase loginUseCase;

	/**
	 * @deprecated 테스트용 페이지. 사용하지 마세요.
	 */
	@Deprecated(since = "v2", forRemoval = true)
	@Hidden
	@GetMapping("/")
	public String publicPage() {
		return "아무나 접근 가능한 페이지 입니다.";
	}

	@Deprecated(since = "v2", forRemoval = true)
	@Hidden
	@GetMapping("/login")
	public void login(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
		response.sendRedirect("/oauth2/authorization/auth0");
	}

	@Override
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
		@RequestBody @Valid LoginRequest request
	) {
		LoginUseCase.LoginResult result = loginUseCase.login(new LoginUseCase.LoginCommand(request.idToken()));

		LoginResponse response;
		if (result.isNewUser()) {
			response = LoginResponse.newUser(
				result.authSub(),
				result.email(),
				result.nickname()
			);
		} else {
			response = LoginResponse.existingMember(result.member().orElseThrow());
		}

		return ResponseEntity.ok(response);
	}

	@Deprecated(since = "v2", forRemoval = true)
	@Hidden
	@GetMapping("/login-success")
	public ResponseEntity<?> loginSuccess(
		@AuthenticationPrincipal OidcUser principal,
		@RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient authorizedClient
	) {
		if (principal == null)
			return ResponseEntity.status(401).body("로그인이 필요합니다.");

		String accessToken = authorizedClient.getAccessToken().getTokenValue();

		return ResponseEntity.ok(Map.of(
			"message", "로그인 성공! 환영합니다, " + principal.getAttribute("name") + "님.",
			"user", principal.getAttributes(),
			"accessToken", accessToken
		));
	}

	// [로그아웃]
	// 로그아웃은 SecurityConfig에 설정된 /api/auth/logout 엔드포인트에서 처리

	// [내 정보 조회 & JWT 검증]
	// JWT 헤더에 담아 요청 시, 유효성을 검증하고 클레임을 반환합니다.
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal Jwt jwt) {
		if (jwt == null) {
			return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
		}
		return ResponseEntity.ok(jwt.getClaims());
	}

	// [토큰 갱신]
	@GetMapping("/refresh")
	public ResponseEntity<?> refreshToken(String token) {
		Map<String, Object> tokenResponse = authService.refreshToken(token);
		return ResponseEntity.ok(tokenResponse);
	}
}
