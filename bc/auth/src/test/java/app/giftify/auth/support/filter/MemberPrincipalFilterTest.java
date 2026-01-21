package app.giftify.auth.support.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import app.giftify.auth.client.MemberApiClient;
import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberPrincipalFilter")
class MemberPrincipalFilterTest {

	@Mock
	private MemberApiClient memberApiClient;

	@Mock
	private FilterChain filterChain;

	private MemberPrincipalFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		filter = new MemberPrincipalFilter(memberApiClient);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("필터 스킵 조건")
	class SkipConditions {

		@Test
		@DisplayName("인증이 없을 때 필터를 스킵한다")
		void skipWhenNoAuthentication() throws Exception {
			// given - SecurityContext는 비어있음

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(memberApiClient);
		}

		@Test
		@DisplayName("principal이 JWT가 아닐 때 스킵한다")
		void skipWhenPrincipalIsNotJwt() throws Exception {
			// given
			Authentication auth = mock(Authentication.class);
			when(auth.getPrincipal()).thenReturn("not-a-jwt");
			SecurityContextHolder.getContext().setAuthentication(auth);

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(memberApiClient);
		}

		@Test
		@DisplayName("이미 MemberAuthenticationToken일 때 스킵한다")
		void skipWhenAlreadyMemberAuthenticationToken() throws Exception {
			// given
			MemberInfo memberInfo = MemberInfo.of(1L, "auth0|test", MemberRole.BUYER, "test@test.com", "tester");
			MemberPrincipal principal = MemberPrincipal.from(memberInfo);
			MemberAuthenticationToken existingAuth = new MemberAuthenticationToken(principal);
			SecurityContextHolder.getContext().setAuthentication(existingAuth);

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(memberApiClient);
		}

		@Test
		@DisplayName("JWT subject가 null일 때 스킵한다")
		void skipWhenJwtSubjectIsNull() throws Exception {
			// given
			Jwt jwt = mock(Jwt.class);
			when(jwt.getSubject()).thenReturn(null);
			JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
			SecurityContextHolder.getContext().setAuthentication(jwtAuth);

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(memberApiClient);
		}

		@Test
		@DisplayName("JWT subject가 비어있을 때 스킵한다")
		void skipWhenJwtSubjectIsBlank() throws Exception {
			// given
			Jwt jwt = mock(Jwt.class);
			when(jwt.getSubject()).thenReturn("   ");
			JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
			SecurityContextHolder.getContext().setAuthentication(jwtAuth);

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(memberApiClient);
		}
	}

	@Nested
	@DisplayName("SecurityContext 보강")
	class EnrichSecurityContext {

		@Test
		@DisplayName("회원을 찾으면 MemberAuthenticationToken으로 교체한다")
		void replaceWithMemberAuthenticationToken() throws Exception {
			// given
			String authSub = "auth0|registered-user";
			Jwt jwt = mock(Jwt.class);
			when(jwt.getSubject()).thenReturn(authSub);
			JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
			SecurityContextHolder.getContext().setAuthentication(jwtAuth);

			MemberInfo memberInfo = MemberInfo.of(1L, authSub, MemberRole.SELLER, "test@test.com", "tester");
			when(memberApiClient.getMemberByAuthSub(authSub)).thenReturn(Optional.of(memberInfo));

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);

			Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
			assertThat(currentAuth).isInstanceOf(MemberAuthenticationToken.class);

			MemberAuthenticationToken memberAuth = (MemberAuthenticationToken) currentAuth;
			assertThat(memberAuth.getPrincipal().memberId()).isEqualTo(1L);
			assertThat(memberAuth.getPrincipal().authSub()).isEqualTo(authSub);
			assertThat(memberAuth.getPrincipal().role()).isEqualTo(MemberRole.SELLER);
		}

		@Test
		@DisplayName("회원을 못찾으면 기존 JWT 인증을 유지한다")
		void keepJwtAuthenticationWhenMemberNotFound() throws Exception {
			// given
			String authSub = "auth0|unregistered-user";
			Jwt jwt = mock(Jwt.class);
			when(jwt.getSubject()).thenReturn(authSub);
			JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
			SecurityContextHolder.getContext().setAuthentication(jwtAuth);

			when(memberApiClient.getMemberByAuthSub(authSub)).thenReturn(Optional.empty());

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);

			Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
			assertThat(currentAuth).isInstanceOf(JwtAuthenticationToken.class);
		}
	}

	@Nested
	@DisplayName("예외 처리")
	class ExceptionHandling {

		@Test
		@DisplayName("예외가 발생해도 필터 체인은 계속 진행된다")
		void continueFilterChainOnException() throws Exception {
			// given
			String authSub = "auth0|error-user";
			Jwt jwt = mock(Jwt.class);
			when(jwt.getSubject()).thenReturn(authSub);
			JwtAuthenticationToken jwtAuth = new JwtAuthenticationToken(jwt);
			SecurityContextHolder.getContext().setAuthentication(jwtAuth);

			when(memberApiClient.getMemberByAuthSub(authSub))
				.thenThrow(new RuntimeException("API error"));

			// when
			filter.doFilterInternal(request, response, filterChain);

			// then
			verify(filterChain).doFilter(request, response);

			// 원래 JWT 인증이 유지됨
			Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
			assertThat(currentAuth).isInstanceOf(JwtAuthenticationToken.class);
		}
	}
}
