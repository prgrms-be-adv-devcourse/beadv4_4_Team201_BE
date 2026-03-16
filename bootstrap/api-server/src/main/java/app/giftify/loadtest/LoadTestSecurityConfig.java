package app.giftify.loadtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import app.giftify.auth.support.filter.MemberPrincipalFilter;
import app.giftify.auth.support.filter.TokenBlacklistFilter;

@Configuration
@Profile("loadtest")
@ConditionalOnProperty(name = "loadtest.mock-auth.enabled", havingValue = "true")
public class LoadTestSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(LoadTestSecurityConfig.class);
    private final MemberPrincipalFilter memberPrincipalFilter;
    private final TokenBlacklistFilter tokenBlacklistFilter;

    public LoadTestSecurityConfig(MemberPrincipalFilter memberPrincipalFilter,
                                  TokenBlacklistFilter tokenBlacklistFilter) {
        this.memberPrincipalFilter = memberPrincipalFilter;
        this.tokenBlacklistFilter = tokenBlacklistFilter;
    }

    // Phase 1 (Mock Auth): X-Test-User-ID 헤더 → DynamicMockAuthFilter가 SecurityContext 설정
    // Phase 2 (Real JWT): Authorization: Bearer → oauth2ResourceServer가 JWT 검증
    // 필터 순서: BearerTokenAuthenticationFilter(자동) → DynamicMockAuthFilter → TokenBlacklist → MemberPrincipal
    @Bean
    @Order(0)
    public SecurityFilterChain loadTestSecurityFilterChain(HttpSecurity http) throws Exception {
        log.warn("=== LoadTest SecurityFilterChain ACTIVE ===");
        log.warn("Phase 1 (Mock Auth) + Phase 2 (JWT) dual mode enabled");

        http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(new DynamicMockAuthFilter(), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(tokenBlacklistFilter, DynamicMockAuthFilter.class)
                .addFilterAfter(memberPrincipalFilter, TokenBlacklistFilter.class);

        return http.build();
    }
}
