package app.giftify.security.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.security.test.TestMemberFactory;
import app.giftify.support.common.security.MemberRole;
import app.giftify.support.common.security.MemberInfo;

@DisplayName("@CurrentMemberId / @CurrentAuthSub 어노테이션 통합 테스트")
class CurrentMemberIdAnnotationTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
			.standaloneSetup(new TestController())
			.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
			.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void setUpAuthentication(Long memberId, String authSub, MemberRole role) {
		MemberInfo memberInfo = MemberInfo.of(memberId, authSub, role, "test@test.com", "tester");
		MemberPrincipal principal = MemberPrincipal.from(memberInfo);
		MemberAuthenticationToken authentication = new MemberAuthenticationToken(principal);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void setUpUnregisteredAuthentication(String authSub) {
		MemberPrincipal principal = MemberPrincipal.forUnregistered(authSub);
		MemberAuthenticationToken authentication = new MemberAuthenticationToken(principal);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Nested
	@DisplayName("@CurrentMemberId")
	class CurrentMemberIdTests {

		@Test
		@DisplayName("@CurrentMemberId로 memberId가 주입된다")
		void injectsMemberId() throws Exception {
			// given
			setUpAuthentication(123L, "auth0|test123", MemberRole.SELLER);

			// when & then
			mockMvc.perform(get("/test/member-id"))
				.andExpect(status().isOk())
				.andExpect(content().string("123"));
		}

		@Test
		@DisplayName("다른 memberId 값도 정확히 주입된다")
		void injectsDifferentMemberId() throws Exception {
			// given
			setUpAuthentication(999L, "auth0|test999", MemberRole.ADMIN);

			// when & then
			mockMvc.perform(get("/test/member-id"))
				.andExpect(status().isOk())
				.andExpect(content().string("999"));
		}
	}

	@Nested
	@DisplayName("@CurrentAuthSub")
	class CurrentAuthSubTests {

		@Test
		@DisplayName("@CurrentAuthSub로 authSub가 주입된다")
		void injectsAuthSub() throws Exception {
			// given
			setUpAuthentication(1L, "auth0|custom-sub-123", MemberRole.BUYER);

			// when & then
			mockMvc.perform(get("/test/auth-sub"))
				.andExpect(status().isOk())
				.andExpect(content().string("auth0|custom-sub-123"));
		}

		@Test
		@DisplayName("다른 authSub 값도 정확히 주입된다")
		void injectsDifferentAuthSub() throws Exception {
			// given
			setUpAuthentication(1L, "google-oauth2|user456", MemberRole.BUYER);

			// when & then
			mockMvc.perform(get("/test/auth-sub"))
				.andExpect(status().isOk())
				.andExpect(content().string("google-oauth2|user456"));
		}
	}

	@Nested
	@DisplayName("미인증 상태")
	class UnauthenticatedTests {

		@Test
		@DisplayName("미인증 시 memberId는 null이 반환된다")
		void returnsNullMemberIdWhenUnauthenticated() throws Exception {
			// given - SecurityContext는 비어있음

			// when & then
			mockMvc.perform(get("/test/member-id-nullable"))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));
		}

		@Test
		@DisplayName("미인증 시 authSub는 null이 반환된다")
		void returnsNullAuthSubWhenUnauthenticated() throws Exception {
			// given - SecurityContext는 비어있음

			// when & then
			mockMvc.perform(get("/test/auth-sub-nullable"))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));
		}
	}

	@Nested
	@DisplayName("미가입 사용자 (Auth0 인증 완료, 서비스 미가입)")
	class UnregisteredUserTests {

		@Test
		@DisplayName("미가입 사용자의 authSub가 정상 주입된다")
		void injectsAuthSubForUnregisteredUser() throws Exception {
			// given
			setUpUnregisteredAuthentication("auth0|unregistered-user");

			// when & then
			mockMvc.perform(get("/test/auth-sub"))
				.andExpect(status().isOk())
				.andExpect(content().string("auth0|unregistered-user"));
		}

		@Test
		@DisplayName("미가입 사용자의 memberId는 null이다")
		void returnsNullMemberIdForUnregisteredUser() throws Exception {
			// given
			setUpUnregisteredAuthentication("auth0|unregistered-user");

			// when & then
			mockMvc.perform(get("/test/member-id-nullable"))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));
		}

		@Test
		@DisplayName("미가입 사용자도 MemberPrincipal 타입이다 (SpEL 평가 성공)")
		void unregisteredUserHasMemberPrincipal() throws Exception {
			// given
			setUpUnregisteredAuthentication("google-oauth2|new-user");

			// when & then - SpEL 평가가 실패하면 예외 발생
			mockMvc.perform(get("/test/auth-sub"))
				.andExpect(status().isOk())
				.andExpect(content().string("google-oauth2|new-user"));
		}
	}

	@Nested
	@DisplayName("역할별 동작")
	class RoleBasedTests {

		@Test
		@DisplayName("BUYER 역할로 memberId가 주입된다")
		void buyerCanAccessMemberId() throws Exception {
			// given
			setUpAuthentication(1L, "auth0|buyer", MemberRole.BUYER);

			// when & then
			mockMvc.perform(get("/test/member-id"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));
		}

		@Test
		@DisplayName("SELLER 역할로 memberId가 주입된다")
		void sellerCanAccessMemberId() throws Exception {
			// given
			setUpAuthentication(2L, "auth0|seller", MemberRole.SELLER);

			// when & then
			mockMvc.perform(get("/test/member-id"))
				.andExpect(status().isOk())
				.andExpect(content().string("2"));
		}

		@Test
		@DisplayName("ADMIN 역할로 memberId가 주입된다")
		void adminCanAccessMemberId() throws Exception {
			// given
			setUpAuthentication(3L, "auth0|admin", MemberRole.ADMIN);

			// when & then
			mockMvc.perform(get("/test/member-id"))
				.andExpect(status().isOk())
				.andExpect(content().string("3"));
		}
	}

	/**
	 * 테스트용 내부 컨트롤러
	 */
	@RestController
	static class TestController {

		@GetMapping("/test/member-id")
		public ResponseEntity<String> getMemberId(@CurrentMemberId Long memberId) {
			return ResponseEntity.ok(String.valueOf(memberId));
		}

		@GetMapping("/test/auth-sub")
		public ResponseEntity<String> getAuthSub(@CurrentAuthSub String authSub) {
			return ResponseEntity.ok(authSub);
		}

		@GetMapping("/test/member-id-nullable")
		public ResponseEntity<String> getMemberIdNullable(@CurrentMemberId Long memberId) {
			return ResponseEntity.ok(String.valueOf(memberId));
		}

		@GetMapping("/test/auth-sub-nullable")
		public ResponseEntity<String> getAuthSubNullable(@CurrentAuthSub String authSub) {
			return ResponseEntity.ok(String.valueOf(authSub));
		}
	}
}
