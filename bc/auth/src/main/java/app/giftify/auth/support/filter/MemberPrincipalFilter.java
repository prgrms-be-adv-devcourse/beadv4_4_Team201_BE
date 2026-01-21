package app.giftify.auth.support.filter;

import app.giftify.auth.client.MemberApiClient;
import app.giftify.shared.domain.vo.MemberInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

			// 1. Optional<Member>를 ifPresent로 세련되게 처리
			memberApiClient.getMemberByAuthSub(authSub).ifPresent(member -> {

				// 2. 인증 객체 생성 (메서드 추출)
				Authentication newAuth = createAuthentication(member);

				// 3. SecurityContext 교체
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(newAuth);
				SecurityContextHolder.setContext(context);

				log.debug("[Filter] SecurityContext updated for Member ID: {}", member.memberId());
			});
		}
	}

	private Authentication createAuthentication(MemberInfo member) {
		// 🚨 중요: DB에 저장된 role이 "SELLER"라면 "ROLE_SELLER"로 변환해야 hasRole('SELLER')가 작동함
		// 만약 member.getRole()이 Enum이라면 .name()을 사용하세요.
		String roleName = "ROLE_" + member.role();
		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

		// 컨트롤러의 @CurrentMemberId가 참조할 객체
		// member.getId(), member.getEmail() 등 실제 Member 객체의 메서드 호출
		MemberPrincipal principal = new MemberPrincipal(
				member.memberId(),
				member.email(),
				authorities
		);

		return new UsernamePasswordAuthenticationToken(
				principal,
				null,
				authorities
		);
	}

	// DTO 역할의 내부 Record
	public record MemberPrincipal(
			Long memberId,
			String email,
			Collection<? extends GrantedAuthority> authorities
	) {}
}