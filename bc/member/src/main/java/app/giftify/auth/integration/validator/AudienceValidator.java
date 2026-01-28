package app.giftify.auth.integration.validator;

import java.util.List;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    // OAuth2 관련 검증
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Audience 존재 - 성공
        List<String> audiences = jwt.getAudience();
        if (audiences.contains(this.audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        // Audience 일치X - 에러 반환
        OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);
        return OAuth2TokenValidatorResult.failure(error);
    }
}
