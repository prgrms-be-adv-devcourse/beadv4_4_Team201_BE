package app.giftify.auth.support.config;

import app.giftify.auth.application.AuthService;
import app.giftify.auth.support.filter.MemberPrincipalFilter;
import app.giftify.auth.support.filter.TokenBlacklistFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final MemberPrincipalFilter memberPrincipalFilter;
    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final AuthService authService;
    private final Environment env;

    @Value("${auth0.audience}")
    private String audience;

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/internal",
                        "/api/v2/auth/login",
                        "/api/v2/home",
                        "/api/auth/login",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/error",
                        "/api/v2/products/{id}",
                        "/api/v2/products/search"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        boolean isLocal = isH2ConsoleAllowed();

        log.info("=== Security Configuration ===");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        log.info("Local environment (H2 enabled): {}", isLocal);
        log.info("H2 Console access: {}", isLocal ? "ALLOWED" : "DENIED");

        http
                // CSRF 비활성화 (JWT 기반 API 중심 구조)
                .csrf(AbstractHttpConfigurer::disable)

                // Frame Options 설정: 개발/로컬 환경에서만 H2 콘솔 접근 허용
                .headers(headers -> {
                    if (isLocal) {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin);
                    } else {
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
                    }
                })

                // 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> {
                    // 루트 경로는 허용
                    auth.requestMatchers("/").permitAll();

                    // H2 콘솔: 로컬 환경에서만 허용
                    if (isLocal) {
                        log.info("Permitting access to /h2-console/**");
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // 나머지 모든 요청은 인증 필요 (공개 엔드포인트는 publicSecurityFilterChain에서 처리)
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
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Token Blacklist 검증 필터 (JWT 검증 후 실행)
                .addFilterAfter(tokenBlacklistFilter, BearerTokenAuthenticationFilter.class)
                // MemberPrincipal 보강 필터 (JWT 검증 후 실행)
                .addFilterAfter(memberPrincipalFilter, BearerTokenAuthenticationFilter.class)
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

    // 로컬 개발 환경인지 확인 (H2 콘솔 접근 권한 제어용)
    // local 프로파일: IDE에서 직접 실행, H2 in-memory DB 사용
    // dev 프로파일: docker-compose 환경, PostgreSQL 사용 -> H2 불필요
    private boolean isH2ConsoleAllowed() {
        String[] activeProfiles = env.getActiveProfiles();

        // [기본값] 프로파일이 설정되지 않은 경우 - 로컬로 간주
        if (activeProfiles.length == 0) {
            log.warn("No active profiles detected. Treating as local environment for safety.");
            return true;
        }

        // local 또는 default 프로파일에서만 H2 콘솔 허용
        // dev 프로파일은 PostgreSQL을 사용하므로 제외
        boolean result = Arrays.stream(activeProfiles)
                .anyMatch(profile ->
                        profile.equalsIgnoreCase("local") ||
                                profile.equalsIgnoreCase("default")
                );

        log.debug("Profile check result - Active: {}, H2 Console Allowed: {}",
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
