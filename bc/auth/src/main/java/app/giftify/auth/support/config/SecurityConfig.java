package app.giftify.auth.support.config;

import app.giftify.auth.core.service.AuthService;
import app.giftify.auth.integration.validator.AudienceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthService authService;

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Bean
    public SecurityFilterChain loginLogoutTest(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Stateless 모드: 서버가 세션(JSESSIONID)을 유지하지 않으므로, 페이지를 새로고침하거나 다시 접속하면 서버는 사용자를 기억하지 못하고 다시 Auth0로 보냄
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/auth/").permitAll() // 메인과 기본 안내 페이지는 허용
                        .requestMatchers("/api/auth/login-success").authenticated() // 로그인이 필요한 페이지 명시
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(authorizationRequestResolver(this.clientRegistrationRepository))
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.authService)
                        )
                        .defaultSuccessUrl("/api/auth/login-success", true)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "GET"))
                        .logoutSuccessHandler(oidcLogoutSuccessHandler()) // Auth0 로그아웃 연동
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
        ;

        return http.build();
    }

    // Auth0 외부 로그아웃을 처리하는 핸들러입니다.
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // 로그아웃 후 리다이렉트될 위치 (Auth0 대시보드에 Allowed Logout URLs로 등록되어 있어야 함)
        logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/api/auth/");
        return logoutSuccessHandler;
    }

    // Auth0 로그인 시 Audience 파라미터를 추가하여 JWT Access Token을 발급받도록 설정
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository repository) {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(repository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(customizer
                -> customizer.additionalParameters(params
                -> params.put("audience", audience)));
        return resolver;
    }

    // 리소스 서버 토큰 검증
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }
}
