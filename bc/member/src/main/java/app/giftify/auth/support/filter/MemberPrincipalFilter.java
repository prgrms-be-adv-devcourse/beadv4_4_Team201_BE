package app.giftify.auth.support.filter;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;

import java.util.Optional;
import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.shared.domain.vo.MemberInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/internal/") || path.equals("/favicon.ico");
	}

	private void enrichSecurityContext() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
			String authSub = jwt.getSubject();

			findMemberByAuthSub(authSub).ifPresent(member -> {
				Authentication newAuth = createAuthentication(member);

				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(newAuth);
				SecurityContextHolder.setContext(context);

				log.debug("[Filter] SecurityContext updated for Member ID: {}", member.memberId());
			});
		}
	}

	private Optional<MemberInfo> findMemberByAuthSub(String authSub) {
		try {
			var response = memberApiClient.getMemberByAuthSub(authSub);
			if (response.getStatusCode().is2xxSuccessful()) {
				return Optional.ofNullable(response.getBody());
			}
			return Optional.empty();
		} catch (Exception e) {
			log.debug("[MemberPrincipalFilter] Member not found for authSub: {}", authSub);
			return Optional.empty();
		}
	}

	private Authentication createAuthentication(MemberInfo member) {
		MemberPrincipal principal = MemberPrincipal.from(member);
		return new MemberAuthenticationToken(principal);
	}
}
