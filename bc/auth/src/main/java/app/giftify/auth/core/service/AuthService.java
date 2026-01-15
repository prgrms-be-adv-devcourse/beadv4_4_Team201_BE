package app.giftify.auth.core.service;

import event.auth.UserAuthenticatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// 핵심 비즈니스 로직 (인증 프로세스 처리, 토큰 유효성 검증, 갱신)
@Service
public class AuthService extends OidcUserService {

    private final ApplicationEventPublisher eventPublisher;
    private final JwtDecoder jwtDecoder;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.auth0.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.auth0.issuer-uri}")
    private String issuerUri;

    // 생성자 주입 (@Lazy를 사용하여 SecurityConfig와의 순환 참조 방지)
    public AuthService(ApplicationEventPublisher eventPublisher, @Lazy JwtDecoder jwtDecoder) {
        this.eventPublisher = eventPublisher;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        processUserAuthentication(oidcUser);
        return oidcUser;
    }

    // [회원가입 트리거]
    // 인증 성공 후 사용자 정보를 추출하여 이벤트를 발행합니다.
    private void processUserAuthentication(OidcUser oidcUser) {
        Map<String, Object> attributes = oidcUser.getAttributes();
        String sub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault("name", "User");

        // 인증 성공 이벤트를 발행하여 멤버 모듈에 알림
        eventPublisher.publishEvent(new UserAuthenticatedEvent(this, sub, email, name));
    }

    // [JWT 검증]
    // 외부에서 받은 토큰의 유효성을 검증하는 로직 (필요 시 추가 구현)
    public boolean validateToken(String token) {
        // JwtDecoder 등을 이용한 검증 로직
        return true;
    }

    // [토큰 갱신]
    // Refresh Token을 이용하여 새로운 Access Token을 발급받는 로직 (Auth0 위임 방식)
    public Map<String, Object> refreshToken(String refreshToken) {
        String url = issuerUri + "oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            throw new OAuth2AuthenticationException("토큰 갱신에 실패했습니다: " + e.getMessage());
        }
    }

    // [토큰 검증 메서드]
    // 다른 모듈이나 내부 로직에서 토큰의 유효성을 명시적으로 확인할 때 사용합니다.
    public Jwt decodeAndValidateToken(String token) {
        try {
            // SecurityConfig에서 정의한 JwtDecoder를 사용하여 검증
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            // 로그를 남기고 예외를 던짐 (AuthExceptionHandler에서 처리)
            throw new OAuth2AuthenticationException("토큰 검증에 실패했습니다: " + e.getMessage());
        }
    }
}
