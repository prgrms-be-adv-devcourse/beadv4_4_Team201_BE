package app.giftify.auth.support.config;

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
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final MemberPrincipalFilter memberPrincipalFilter;
    private final TokenBlacklistFilter tokenBlacklistFilter;
    private final Environment env;

    @Value("${auth0.audience}")
    private String audience;

    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/internal/**",
                        "/api/v2/auth/login",
                        "/api/v2/home",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/error",
                        "/api/v2/products/{id:\\d+}",
                        "/api/v2/products/search",
                        "/api/v2/wishlists/search",
                        "/api/v2/wishlists/*" // NOTE :: 단일 세그먼트 전체 공개 주의, wishlistController apiV2 올릴 때 수정하여 다시 확인하기
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
                // JWT 리소스 서버 설정
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                // Token Blacklist 검증 필터 (JWT 검증 후 실행)
                .addFilterAfter(tokenBlacklistFilter, BearerTokenAuthenticationFilter.class)
                // MemberPrincipal 보강 필터 (JWT 검증 후 실행)
                .addFilterAfter(memberPrincipalFilter, BearerTokenAuthenticationFilter.class);

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
}
