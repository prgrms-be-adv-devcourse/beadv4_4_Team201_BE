package app.giftify.security.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalApiAuthFilter extends OncePerRequestFilter {

	private static final String HEADER = "X-Internal-Api-Key";
	private static final String INTERNAL_PREFIX = "/api/internal/";

	private final byte[] expected;

	public InternalApiAuthFilter(String secret) {
		if (!StringUtils.hasText(secret)) {
			throw new IllegalArgumentException("internal api key must not be blank");
		}
		this.expected = secret.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws ServletException, IOException {
		String uri = request.getRequestURI();
		if (uri == null || !uri.startsWith(INTERNAL_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		String provided = request.getHeader(HEADER);
		if (provided == null
			|| !MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		chain.doFilter(request, response);
	}
}
