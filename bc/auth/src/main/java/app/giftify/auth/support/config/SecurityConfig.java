package app.giftify.auth.support.config;

import app.giftify.auth.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final AuthService authService;
    private final Environment env;

    @Value("${auth0.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevOrLocal = isH2ConsoleAllowed();

        log.info("=== Security Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        log.info("Dev or Local environment: {}", isDevOrLocal);
        log.info("H2 Console access: {}", isDevOrLocal ? "ALLOWED" : "DENIED");

        http
                // CSRF 비활성화 (JWT 기반 API 중심 구조)
                .csrf(csrf -> csrf.disable())

                // Frame Options 설정: 개발/로컬 환경에서만 H2 콘솔 접근 허용
                .headers(headers -> {
                    if (isDevOrLocal) {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    } else {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                    }
                })

                // 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> {
                    // 공개 엔드포인트
                    auth.requestMatchers("/", "/api/auth/login").permitAll();

                    // H2 콘솔: 개발/로컬 환경에서만 허용
                    if (isDevOrLocal) {
                        log.info("Permitting access to /h2-console/**");
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // 나머지 모든 요청은 인증 필요
                    auth.anyRequest().authenticated();
                })

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authEndpoint -> authEndpoint
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository)
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.authService)
                        )
                        .defaultSuccessUrl("/api/auth/login-success", true)
                )

                // JWT 리소스 서버 설정
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "GET"))
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    // 현재 환경이 개발 또는 로컬 환경인지 확인
    // H2 콘솔 접근 권한을 제어하기 위해 사용
    private boolean isH2ConsoleAllowed() {
        String[] activeProfiles = env.getActiveProfiles();

        // [기본값] 프로파일이 설정되지 않은 경우
        if (activeProfiles.length == 0) {
            log.warn("No active profiles detected. Treating as local environment for safety.");
            return true;
        }

        // local, dev, default 프로파일 중 하나라도 활성화되어 있으면 true
        boolean result = Arrays.stream(activeProfiles)
                .anyMatch(profile ->
                        profile.equalsIgnoreCase("local") ||
                                profile.equalsIgnoreCase("dev") ||
                                profile.equalsIgnoreCase("default")
                );

        log.debug("Profile check result - Active: {}, Is Dev/Local: {}",
                Arrays.toString(activeProfiles), result);

        return result;
    }

    // Auth0 로그아웃 핸들러
    // Auth0 로그아웃 페이지로 리다이렉트하여 완전한 로그아웃 처리
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // 로그아웃 후 리다이렉트 URL (Auth0 대시보드의 Allowed Logout URLs에 등록 필요)
        handler.setPostLogoutRedirectUri("{baseUrl}/api/auth/");

        return handler;
    }

    // Auth0 인증 요청 커스터마이징
    // JWT Access Token 발급을 위해 audience 파라미터 추가
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository repository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repository, "/oauth2/authorization");

        // audience 파라미터 추가
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params ->
                        params.put("audience", audience)
                )
        );

        return resolver;
    }
}