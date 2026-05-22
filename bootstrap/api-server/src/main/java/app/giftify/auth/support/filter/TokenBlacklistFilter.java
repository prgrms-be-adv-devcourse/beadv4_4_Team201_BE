package app.giftify.auth.support.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import app.giftify.auth.application.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
/**
 * <li> Filter 순서: BearerTokenAuthenticationFilter → TokenBlacklistFilter → MemberPrincipalFilter
 * <li> Early Return: 무효화된 토큰이면 즉시 401 응답, filterChain.doFilter() 호출하지 않음
 * <li> ObjectMapper 재사용: Spring이 관리하는 Bean을 주입받아 성능 최적화
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistFilter extends OncePerRequestFilter {
	private static final Logger log = LoggerFactory.getLogger(TokenBlacklistFilter.class);


	private final TokenBlacklistService blacklistService;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getPrincipal() instanceof Jwt jwt && blacklistService.isTokenRevoked(jwt)) {
			log.info("[TokenBlacklistFilter] Rejected revoked token for request: {}", request.getRequestURI());
			sendErrorResponse(response);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void sendErrorResponse(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		Map<String, String> errorBody = Map.of(
			"error", "TOKEN_REVOKED",
			"message", "This token has been revoked"
		);

		objectMapper.writeValue(response.getWriter(), errorBody);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/api/v2/auth/login")
			|| path.startsWith("/actuator/health")
			|| path.startsWith("/error");
	}
}
