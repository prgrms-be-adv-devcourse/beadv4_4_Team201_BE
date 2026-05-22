package app.giftify.auth.adapter.inbound.web;

import app.giftify.auth.adapter.inbound.web.dto.LoginRequest;
import app.giftify.auth.adapter.inbound.web.dto.LoginResponse;
import app.giftify.auth.adapter.inbound.web.dto.LogoutRequest;
import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.auth.application.inbound.LogoutUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController implements AuthV2ApiSpec {

	private final LoginUseCase loginUseCase;
	private final LogoutUseCase logoutUseCase;

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

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
		@RequestHeader("Authorization") String authorization,
		@RequestBody(required = false) LogoutRequest request
	) {
		String token = authorization.replace("Bearer ", "");
		String refreshToken = (request != null) ? request.refreshToken() : null;

		logoutUseCase.logout(new LogoutUseCase.LogoutCommand(token, refreshToken));
		return ResponseEntity.noContent().build();
	}
}
