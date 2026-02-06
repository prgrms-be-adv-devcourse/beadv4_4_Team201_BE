package app.giftify.security.common.config;

import app.giftify.security.common.validator.AudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@AutoConfiguration
@Import(SharedSecurityConfig.class)
public class SharedSecurityAutoConfiguration {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    /**
     * Access token용 JwtDecoder (API audience 검증).
     * Spring Security OAuth2 Resource Server가 자동으로 사용.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        return createJwtDecoder(audience);
    }

    /**
     * ID token용 JwtDecoder (client_id를 audience로 검증).
     * 로그인 시 프론트엔드가 보낸 id_token 검증에 사용.
     */
    @Bean
    public JwtDecoder idTokenDecoder() {
        return createJwtDecoder(clientId);
    }

    private JwtDecoder createJwtDecoder(String expectedAudience) {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(expectedAudience);
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
    }
}
