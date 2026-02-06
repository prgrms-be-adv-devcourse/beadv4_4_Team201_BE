package app.giftify.auth.adapter.inbound.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.auth.adapter.inbound.web.dto.LoginRequest;
import app.giftify.auth.adapter.inbound.web.dto.LoginResponse;
import app.giftify.auth.application.AuthService;
import app.giftify.auth.application.inbound.LoginUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController implements AuthV2ApiSpec {

	private final AuthService authService;
	private final LoginUseCase loginUseCase;

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

	@Override
	@GetMapping("/me")
	public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal Jwt jwt) {
		if (jwt == null) {
			return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
		}
		return ResponseEntity.ok(jwt.getClaims());
	}

	@Override
	@GetMapping("/refresh")
	public ResponseEntity<?> refreshToken(@RequestParam String token) {
		Map<String, Object> tokenResponse = authService.refreshToken(token);
		return ResponseEntity.ok(tokenResponse);
	}
}
