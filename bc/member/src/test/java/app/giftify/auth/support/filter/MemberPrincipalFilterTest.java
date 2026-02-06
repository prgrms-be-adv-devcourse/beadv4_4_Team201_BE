package app.giftify.auth.support.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberPrincipalFilter")
class MemberPrincipalFilterTest {

	@Mock
	private MemberApiClient memberApiClient;

	private MemberPrincipalFilter filter;

	@BeforeEach
	void setUp() {
		filter = new MemberPrincipalFilter(memberApiClient);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private Jwt createMockJwt(String subject) {
		return new Jwt(
			"token-value",
			Instant.now(),
			Instant.now().plusSeconds(3600),
			Map.of("alg", "RS256"),
			Map.of("sub", subject)
		);
	}

	private void setUpJwtAuthentication(String authSub) {
		Jwt jwt = createMockJwt(authSub);
		JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Nested
	@DisplayName("회원이 존재하는 경우")
	class RegisteredMemberTests {

		@Test
		@DisplayName("MemberPrincipal로 SecurityContext가 업데이트된다")
		void updatesSecurityContextWithMemberPrincipal() throws Exception {
			// given
			String authSub = "auth0|registered-user";
			setUpJwtAuthentication(authSub);

			MemberInfo memberInfo = MemberInfo.of(123L, authSub, MemberRole.BUYER, "test@test.com", "tester");
			given(memberApiClient.getMemberByAuthSub(authSub))
				.willReturn(ResponseEntity.ok(memberInfo));

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/api/v2/products");
			MockHttpServletResponse response = new MockHttpServletResponse();
			MockFilterChain filterChain = new MockFilterChain();

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			var authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication.getPrincipal()).isInstanceOf(MemberPrincipal.class);

			MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
			assertThat(principal.memberId()).isEqualTo(123L);
			assertThat(principal.authSub()).isEqualTo(authSub);
			assertThat(principal.isRegistered()).isTrue();
		}
	}

	@Nested
	@DisplayName("미가입 사용자인 경우")
	class UnregisteredUserTests {

		@Test
		@DisplayName("미가입 사용자도 MemberPrincipal로 래핑된다")
		void wrapsUnregisteredUserWithMemberPrincipal() throws Exception {
			// given
			String authSub = "auth0|unregistered-user";
			setUpJwtAuthentication(authSub);

			// 회원 조회 실패 (404 또는 예외)
			given(memberApiClient.getMemberByAuthSub(authSub))
				.willThrow(new RuntimeException("Member not found"));

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/api/v2/members/check-registration");
			MockHttpServletResponse response = new MockHttpServletResponse();
			MockFilterChain filterChain = new MockFilterChain();

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			var authentication = SecurityContextHolder.getContext().getAuthentication();
			assertThat(authentication.getPrincipal()).isInstanceOf(MemberPrincipal.class);

			MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
			assertThat(principal.memberId()).isNull();
			assertThat(principal.authSub()).isEqualTo(authSub);
			assertThat(principal.isRegistered()).isFalse();
		}

		@Test
		@DisplayName("미가입 사용자의 authSub는 정상적으로 접근 가능하다")
		void authSubIsAccessibleForUnregisteredUser() throws Exception {
			// given
			String authSub = "google-oauth2|new-user";
			setUpJwtAuthentication(authSub);

			given(memberApiClient.getMemberByAuthSub(authSub))
				.willReturn(ResponseEntity.notFound().build());

			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/api/v2/members/signup");
			MockHttpServletResponse response = new MockHttpServletResponse();
			MockFilterChain filterChain = new MockFilterChain();

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			var authentication = SecurityContextHolder.getContext().getAuthentication();
			MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();

			// SpEL 평가 시 사용되는 메서드 호출 확인
			assertThat(principal.authSub()).isEqualTo(authSub);
			assertThat(principal.memberId()).isNull();
		}
	}

	@Nested
	@DisplayName("필터 제외 경로")
	class ShouldNotFilterTests {

		@Test
		@DisplayName("/api/internal/ 경로는 필터를 건너뛴다")
		void skipsInternalApiPath() {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/api/internal/members/123");

			assertThat(filter.shouldNotFilter(request)).isTrue();
		}

		@Test
		@DisplayName("/favicon.ico는 필터를 건너뛴다")
		void skipsFavicon() {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/favicon.ico");

			assertThat(filter.shouldNotFilter(request)).isTrue();
		}

		@Test
		@DisplayName("일반 API 경로는 필터를 적용한다")
		void appliesFilterForNormalApiPath() {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.setRequestURI("/api/v2/products");

			assertThat(filter.shouldNotFilter(request)).isFalse();
		}
	}
}
