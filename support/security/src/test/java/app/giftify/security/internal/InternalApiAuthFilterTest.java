package app.giftify.security.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class InternalApiAuthFilterTest {

	private static final String VALID_KEY = "test-internal-key-32-bytes-12345";
	private static final String HEADER_NAME = "X-Internal-Api-Key";

	private InternalApiAuthFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChain chain;

	@BeforeEach
	void setUp() {
		filter = new InternalApiAuthFilter(VALID_KEY);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = new MockFilterChain();
	}

	@Nested
	@DisplayName("Internal API 경로 - 키 검증")
	class InternalPathAuth {

		@Test
		@DisplayName("올바른 키가 헤더에 있으면 체인이 진행된다")
		void valid_key_passes_through() throws ServletException, IOException {
			request.setRequestURI("/api/internal/payments/1");
			request.addHeader(HEADER_NAME, VALID_KEY);

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(200);
		}

		@Test
		@DisplayName("헤더가 누락되면 401 Unauthorized")
		void missing_header_returns_401() throws ServletException, IOException {
			request.setRequestURI("/api/internal/payments/1");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("키가 다르면 401 Unauthorized")
		void wrong_key_returns_401() throws ServletException, IOException {
			request.setRequestURI("/api/internal/payments/1");
			request.addHeader(HEADER_NAME, "wrong-key-value-that-is-32-bytes");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("키 길이가 달라도 상수시간 비교로 안전하게 401 반환")
		void short_key_returns_401_constant_time() throws ServletException, IOException {
			request.setRequestURI("/api/internal/payments/1");
			request.addHeader(HEADER_NAME, "x");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(401);
		}

		@Test
		@DisplayName("빈 헤더 값은 401")
		void empty_header_returns_401() throws ServletException, IOException {
			request.setRequestURI("/api/internal/payments/1");
			request.addHeader(HEADER_NAME, "");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(401);
		}
	}

	@Nested
	@DisplayName("Internal API 외 경로 - 통과")
	class ExternalPathPassthrough {

		@Test
		@DisplayName("일반 API 경로는 키 없이도 통과")
		void external_api_passes_without_key() throws ServletException, IOException {
			request.setRequestURI("/api/members/me");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(200);
		}

		@Test
		@DisplayName("/api/internal 과 유사하지만 다른 prefix 는 통과")
		void similar_prefix_does_not_match() throws ServletException, IOException {
			request.setRequestURI("/api/internalization/locale");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(200);
		}

		@Test
		@DisplayName("actuator 경로는 통과")
		void actuator_passes_through() throws ServletException, IOException {
			request.setRequestURI("/actuator/health");

			filter.doFilter(request, response, chain);

			assertThat(response.getStatus()).isEqualTo(200);
		}
	}

	@Nested
	@DisplayName("생성자 검증")
	class ConstructorValidation {

		@Test
		@DisplayName("null 시크릿이면 IllegalArgumentException")
		void null_secret_throws() {
			assertThatThrownBy(() -> new InternalApiAuthFilter(null))
				.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("빈 시크릿이면 IllegalArgumentException (fail-safe)")
		void blank_secret_throws() {
			assertThatThrownBy(() -> new InternalApiAuthFilter("   "))
				.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
