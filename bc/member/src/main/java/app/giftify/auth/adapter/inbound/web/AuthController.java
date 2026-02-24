package app.giftify.auth.adapter.inbound.web;

import app.giftify.auth.adapter.inbound.web.dto.LoginRequest;
import app.giftify.auth.adapter.inbound.web.dto.LoginResponse;
import app.giftify.auth.application.TokenBlacklistService;
import app.giftify.auth.application.inbound.LoginUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController implements AuthV2ApiSpec {

    private final LoginUseCase loginUseCase;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtDecoder jwtDecoder;

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
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(token);

        Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAt());
        if (ttl.isNegative()) {
            ttl = Duration.ZERO;
        }

        tokenBlacklistService.revokeToken(jwt.getId(), ttl, "logout");
        return ResponseEntity.noContent().build();
    }
}
