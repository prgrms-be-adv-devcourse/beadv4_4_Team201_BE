package app.giftify.auth.support.filter;

import java.io.IOException;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.shared.domain.vo.MemberInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberPrincipalFilter extends OncePerRequestFilter {

	private final MemberApiClient memberApiClient;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
									@NonNull HttpServletResponse response,
									@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			enrichSecurityContext();
		} catch (Exception e) {
			log.warn("[MemberPrincipalFilter] MemberPrincipal을 통한 SecurityContext 보강에 실패하였습니다.", e);
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/internal/") || path.equals("/favicon.ico");
	}

	private void enrichSecurityContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			log.debug("[MemberPrincipalFilter] SecurityContext 정보가 없습니다. - skipping enrichment");
			return;
		}

		if (!(auth.getPrincipal() instanceof Jwt jwt)) {
			Object principal = auth.getPrincipal();
			log.debug("[MemberPrincipalFilter] Principal 이 Jwt 가 아닙니다. (type: {}) - skipping enrichment",
				principal != null ? principal.getClass().getSimpleName() : "null");
			return;
		}

		String authSub = jwt.getSubject();

		// 회원 조회: 있으면 MemberInfo, 없으면 미가입자용 MemberInfo
		MemberInfo memberInfo = findMemberByAuthSub(authSub)
			.orElseGet(() -> MemberInfo.forUnregistered(authSub));

		MemberPrincipal principal = MemberPrincipal.from(memberInfo);
		Authentication newAuth = new MemberAuthenticationToken(principal);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(newAuth);
		SecurityContextHolder.setContext(context);

		log.debug("[MemberPrincipalFilter] SecurityContext 업데이트 - registered: {}, authSub: {}",
			memberInfo.isRegistered(), authSub);
	}

	private Optional<MemberInfo> findMemberByAuthSub(String authSub) {
		try {
			var response = memberApiClient.getMemberByAuthSub(authSub);
			if (response.getStatusCode().is2xxSuccessful()) {
				return Optional.ofNullable(response.getBody());
			}
			return Optional.empty();
		} catch (Exception e) {
			log.debug("[MemberPrincipalFilter] authSub에서 Member를 추출할수 없었습니다. : {}", authSub);
			return Optional.empty();
		}
	}
}
