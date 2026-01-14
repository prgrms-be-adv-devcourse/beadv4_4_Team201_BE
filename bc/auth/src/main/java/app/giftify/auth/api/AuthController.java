package app.giftify.auth.api;

import app.giftify.auth.core.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 외부 API 접점(로그인 확인, 내 정보 조회, 토큰 갱신)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String publicPage() {
        return "아무나 접근 가능한 페이지 입니다.";
    }

    // [로그인 확인 및 토큰 반환]
    // Auth0 로그인이 성공했는지 확인하고 발급된 Access Token을 반환합니다.
    @GetMapping("/login-success")
    public ResponseEntity<?> loginSuccess(
            @AuthenticationPrincipal OidcUser principal,
            @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient authorizedClient
    ) {
        if (principal == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

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
    public ResponseEntity<String> refreshToken(String token) {
        String newToken = authService.refreshToken(token);
        return ResponseEntity.ok(newToken);
    }
}