package app.giftify.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

// 핵심 비즈니스 로직 (인증 프로세스 처리, 토큰 유효성 검증)
@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    private final JwtDecoder jwtDecoder;
    private final JwtDecoder idTokenDecoder;

    // 생성자 주입 (@Lazy를 사용하여 SecurityConfig와의 순환 참조 방지)
    public AuthService(
            @Lazy JwtDecoder jwtDecoder,
            @Lazy @Qualifier("idTokenDecoder") JwtDecoder idTokenDecoder
    ) {
        this.jwtDecoder = jwtDecoder;
        this.idTokenDecoder = idTokenDecoder;
    }

    // [JWT 검증]
    // 외부에서 받은 토큰의 유효성을 검증합니다.
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // [id_token 검증 메서드]
    // 로그인 엔드포인트에서 프론트엔드가 보낸 id_token을 검증할 때 사용합니다.
    // id_token은 client_id를 audience로 가지므로 idTokenDecoder를 사용합니다.
    public Jwt decodeAndValidateToken(String token) {
        try {
            return idTokenDecoder.decode(token);
        } catch (JwtException e) {
            log.error("ID Token validation failed", e);
            throw new OAuth2AuthenticationException("토큰 검증에 실패했습니다.");
        }
    }
}
