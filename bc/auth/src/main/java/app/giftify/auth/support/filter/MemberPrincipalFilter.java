package app.giftify.auth.support.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.giftify.auth.client.MemberApiClient;
import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 검증 후 MemberPrincipal로 SecurityContext를 보강하는 필터.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPrincipalFilter extends OncePerRequestFilter {

	private final MemberApiClient memberApiClient;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			enrichSecurityContext();
		} catch (Exception e) {
			log.warn("Failed to enrich SecurityContext with MemberPrincipal", e);
		}

		filterChain.doFilter(request, response);
	}

	private void enrichSecurityContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// 1. JWT 인증이 없으면 스킵
		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			return;
		}

		// 2. 이미 MemberAuthenticationToken이면 스킵
		if (auth instanceof MemberAuthenticationToken) {
			return;
		}

		// 3. authSub 추출
		String authSub = jwt.getSubject();
		if (authSub == null || authSub.isBlank()) {
			log.warn("JWT subject (authSub) is empty");
			return;
		}

		// 4. HTTP로 회원 정보 조회 → MemberPrincipal 생성 → SecurityContext 업데이트
		memberApiClient.getMemberByAuthSub(authSub)
			.map(MemberPrincipal::from)
			.ifPresent(principal -> {
				Authentication enrichedAuth = new MemberAuthenticationToken(principal);
				SecurityContextHolder.getContext().setAuthentication(enrichedAuth);
				log.debug("SecurityContext enriched for memberId: {}", principal.memberId());
			});
	}
}
