package app.giftify.security.common.resolver;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import app.giftify.security.common.MemberAuthenticationToken;
import app.giftify.security.common.MemberPrincipal;
import app.giftify.security.common.context.AuthenticatedMember;
import app.giftify.security.test.TestMemberFactory;
import app.giftify.shared.domain.type.MemberRole;

@DisplayName("AuthenticatedMemberArgumentResolver")
class AuthenticatedMemberArgumentResolverTest {

	private AuthenticatedMemberArgumentResolver resolver;

	@BeforeEach
	void setUp() {
		resolver = new AuthenticatedMemberArgumentResolver();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Nested
	@DisplayName("supportsParameter")
	class SupportsParameter {

		@Test
		@DisplayName("@AuthenticatedMember String 타입을 지원한다")
		void supportsStringParameter() throws NoSuchMethodException {
			// given
			MethodParameter parameter = getParameter("stringParam", String.class);

			// when
			boolean supports = resolver.supportsParameter(parameter);

			// then
			assertThat(supports).isTrue();
		}

		@Test
		@DisplayName("@AuthenticatedMember Long 타입을 지원한다")
		void supportsLongParameter() throws NoSuchMethodException {
			// given
			MethodParameter parameter = getParameter("longParam", Long.class);

			// when
			boolean supports = resolver.supportsParameter(parameter);

			// then
			assertThat(supports).isTrue();
		}

		@Test
		@DisplayName("@AuthenticatedMember MemberPrincipal 타입을 지원한다")
		void supportsMemberPrincipalParameter() throws NoSuchMethodException {
			// given
			MethodParameter parameter = getParameter("principalParam", MemberPrincipal.class);

			// when
			boolean supports = resolver.supportsParameter(parameter);

			// then
			assertThat(supports).isTrue();
		}

		@Test
		@DisplayName("어노테이션 없는 파라미터는 지원하지 않는다")
		void doesNotSupportWithoutAnnotation() throws NoSuchMethodException {
			// given
			MethodParameter parameter = getParameter("noAnnotationParam", String.class);

			// when
			boolean supports = resolver.supportsParameter(parameter);

			// then
			assertThat(supports).isFalse();
		}

		@Test
		@DisplayName("지원하지 않는 타입(Integer)은 지원하지 않는다")
		void doesNotSupportUnsupportedType() throws NoSuchMethodException {
			// given
			MethodParameter parameter = getParameter("integerParam", Integer.class);

			// when
			boolean supports = resolver.supportsParameter(parameter);

			// then
			assertThat(supports).isFalse();
		}
	}

	@Nested
	@DisplayName("resolveArgument - MemberPrincipal 인증")
	class ResolveArgumentWithMemberPrincipal {

		@Test
		@DisplayName("Long 타입에 memberId를 반환한다")
		void resolveMemberId() throws Exception {
			// given
			Long expectedMemberId = 123L;
			setUpMemberAuthentication(expectedMemberId, MemberRole.SELLER);
			MethodParameter parameter = getParameter("longParam", Long.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isEqualTo(expectedMemberId);
		}

		@Test
		@DisplayName("String 타입에 authSub를 반환한다")
		void resolveAuthSub() throws Exception {
			// given
			String expectedAuthSub = "auth0|custom123";
			setUpMemberAuthenticationWithAuthSub(1L, expectedAuthSub, MemberRole.BUYER);
			MethodParameter parameter = getParameter("stringParam", String.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isEqualTo(expectedAuthSub);
		}

		@Test
		@DisplayName("MemberPrincipal 타입에 principal을 반환한다")
		void resolveMemberPrincipal() throws Exception {
			// given
			MemberPrincipal expectedPrincipal = setUpMemberAuthentication(1L, MemberRole.ADMIN);
			MethodParameter parameter = getParameter("principalParam", MemberPrincipal.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isEqualTo(expectedPrincipal);
		}
	}

	@Nested
	@DisplayName("resolveArgument - JWT 인증")
	class ResolveArgumentWithJwt {

		@Test
		@DisplayName("String 타입에 JWT subject를 반환한다")
		void resolveJwtSubject() throws Exception {
			// given
			String expectedSubject = "auth0|jwt-user";
			setUpJwtAuthentication(expectedSubject);
			MethodParameter parameter = getParameter("stringParam", String.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isEqualTo(expectedSubject);
		}

		@Test
		@DisplayName("Long 타입에 null을 반환한다 (미가입자)")
		void resolveLongReturnsNull() throws Exception {
			// given
			setUpJwtAuthentication("auth0|unregistered");
			MethodParameter parameter = getParameter("longParam", Long.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isNull();
		}

		@Test
		@DisplayName("MemberPrincipal 타입에 null을 반환한다 (미가입자)")
		void resolveMemberPrincipalReturnsNull() throws Exception {
			// given
			setUpJwtAuthentication("auth0|unregistered");
			MethodParameter parameter = getParameter("principalParam", MemberPrincipal.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isNull();
		}
	}

	@Nested
	@DisplayName("resolveArgument - 인증 없음")
	class ResolveArgumentWithoutAuthentication {

		@Test
		@DisplayName("authentication이 null일 때 null을 반환한다")
		void resolveReturnsNullWhenNoAuthentication() throws Exception {
			// given - SecurityContext는 비어있음
			MethodParameter parameter = getParameter("stringParam", String.class);

			// when
			Object result = resolver.resolveArgument(parameter, null, null, null);

			// then
			assertThat(result).isNull();
		}
	}

	// ========== Helper Methods ==========

	private MemberPrincipal setUpMemberAuthentication(Long memberId, MemberRole role) {
		MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(memberId, role);
		MemberAuthenticationToken authentication = new MemberAuthenticationToken(principal);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return principal;
	}

	private void setUpMemberAuthenticationWithAuthSub(Long memberId, String authSub, MemberRole role) {
		MemberPrincipal principal = TestMemberFactory.createMemberPrincipal(memberId, authSub, role);
		MemberAuthenticationToken authentication = new MemberAuthenticationToken(principal);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setUpJwtAuthentication(String subject) {
		Jwt jwt = mock(Jwt.class);
		when(jwt.getSubject()).thenReturn(subject);
		JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private MethodParameter getParameter(String methodName, Class<?> paramType) throws NoSuchMethodException {
		Method method = TestController.class.getMethod(methodName, paramType);
		return new MethodParameter(method, 0);
	}

	/**
	 * 테스트용 컨트롤러 - 리플렉션을 통해 MethodParameter를 추출하기 위한 용도.
	 * 메서드 본문은 호출되지 않으며, 파라미터 어노테이션 메타데이터만 사용됨.
	 */
	@SuppressWarnings("unused")
	static class TestController {
		public void stringParam(@AuthenticatedMember String authSub) { /* 리플렉션용 - 호출되지 않음 */ }
		public void longParam(@AuthenticatedMember Long memberId) { /* 리플렉션용 - 호출되지 않음 */ }
		public void principalParam(@AuthenticatedMember MemberPrincipal principal) { /* 리플렉션용 - 호출되지 않음 */ }
		public void noAnnotationParam(String value) { /* 리플렉션용 - 호출되지 않음 */ }
		public void integerParam(@AuthenticatedMember Integer value) { /* 리플렉션용 - 호출되지 않음 */ }
	}
}
