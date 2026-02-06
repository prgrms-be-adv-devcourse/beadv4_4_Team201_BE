package app.giftify.security.common.validator;

import java.util.List;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * JWT audience 클레임 검증기.
 *
 * <p>Auth0 공식 문서 권장 패턴을 따르다 보니 작성하게 되었읍니다.
 * Spring Security의 {@code JwtClaimValidator}로 대체 가능하나,
 * 명시적인 에러 메시지와 클래스명을 통한 의도 표현을 위해 커스텀 구현 유지.</p>
 *
 * @see <a href="https://auth0.com/docs/quickstart/backend/java-spring-security5">Auth0 Spring Security Guide</a>
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audiences = jwt.getAudience();
        if (audiences.contains(this.audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
