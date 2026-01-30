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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginUseCase loginUseCase;

    @GetMapping("/")
    public String publicPage() {
        return "아무나 접근 가능한 페이지 입니다.";
    }

    // 기존 레거시 코드: OAuth2 리다이렉트용, Auth0 로그인 페이지로 리다이렉트
    @GetMapping("/login")
    public void login(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect("/oauth2/authorization/auth0");
    }

    /**
     * SPA SDK용 로그인 엔드포인트.
     * idToken을 검증하고 회원 정보와 가입 여부를 반환합니다.
     *
     * @param request idToken이 담긴 요청
     * @return 회원 정보와 isNewUser 플래그
     */
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

    // [로그인 확인 및 토큰 반환]
    // Auth0 로그인이 성공했는지 확인하고 발급된 Access Token을 반환합니다.
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
