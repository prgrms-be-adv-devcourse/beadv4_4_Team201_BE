package app.giftify.loadtest;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Phase 1 (Isolated Benchmark) 전용 Mock 인증 필터.
// JWT 검증을 우회하고, k6 요청 헤더에서 사용자 정보를 추출하여 SecurityContext에 주입한다.
// LoadTestSecurityConfig의 @Profile("loadtest") + @ConditionalOnProperty로만 활성화되므로
// prod 단독 실행에서는 절대 등록되지 않는다.
public class DynamicMockAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DynamicMockAuthFilter.class);

    // k6 스크립트에서 주입하는 헤더. 실제 Auth0 JWT 대신 이 헤더로 사용자를 식별한다.
    private static final String HEADER_USER_ID = "X-Test-User-ID";
    private static final String HEADER_USER_ROLE = "X-Test-User-ROLE";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String userIdHeader = request.getHeader(HEADER_USER_ID);

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            Long memberId = Long.parseLong(userIdHeader);
            MemberRole role = parseRole(request.getHeader(HEADER_USER_ROLE));

            // MemberPrincipalFilter와 동일한 MemberAuthenticationToken을 생성하여
            // @AuthenticationPrincipal MemberPrincipal이 컨트롤러에서 정상 동작하도록 조작하는 부분
            String authSub = "loadtest|" + memberId;
            MemberInfo memberInfo = MemberInfo.of(memberId, authSub, role, null, null);
            MemberPrincipal principal = MemberPrincipal.from(memberInfo);
            MemberAuthenticationToken token = new MemberAuthenticationToken(principal);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(token);
            SecurityContextHolder.setContext(context);
        }
        // X-Test-User-ID 헤더가 없으면 인증 없이 통과 — 공개 엔드포인트 대응

        filterChain.doFilter(request, response);
    }

    private MemberRole parseRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            return MemberRole.BUYER;
        }
        try {
            return MemberRole.valueOf(roleHeader.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role header: {}, falling back to BUYER", roleHeader);
            return MemberRole.BUYER;
        }
    }
}
