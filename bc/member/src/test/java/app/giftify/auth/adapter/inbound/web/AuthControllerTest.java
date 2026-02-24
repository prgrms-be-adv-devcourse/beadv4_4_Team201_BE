package app.giftify.auth.adapter.inbound.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.auth.application.inbound.LogoutUseCase;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;

@DisplayName("AuthController 테스트")
class AuthControllerTest {

	private MockMvc mockMvc;
	private LoginUseCase loginUseCase;
	private LogoutUseCase logoutUseCase;

	@BeforeEach
	void setUp() {
		loginUseCase = org.mockito.Mockito.mock(LoginUseCase.class);
		logoutUseCase = org.mockito.Mockito.mock(LogoutUseCase.class);

		mockMvc = MockMvcBuilders
			.standaloneSetup(new AuthController(loginUseCase, logoutUseCase))
			.build();
	}

	@Nested
	@DisplayName("POST /api/v2/auth/login")
	class LoginTests {

		@Test
		@DisplayName("신규 사용자 로그인 성공")
		void login_NewUser_Success() throws Exception {
			// given
			String idToken = "valid.id.token";
			String authSub = "auth0|newuser";
			String email = "new@example.com";
			String nickname = "신규유저";

			LoginUseCase.LoginResult result = LoginUseCase.LoginResult.newUser(authSub, email, nickname);
			given(loginUseCase.login(any(LoginUseCase.LoginCommand.class))).willReturn(result);

			// when & then
			mockMvc.perform(post("/api/v2/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"idToken\":\"" + idToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isNewUser").value(true))
				.andExpect(jsonPath("$.authSub").value(authSub))
				.andExpect(jsonPath("$.email").value(email));
		}

		@Test
		@DisplayName("기존 회원 로그인 성공")
		void login_ExistingMember_Success() throws Exception {
			// given
			String idToken = "valid.id.token";
			String authSub = "auth0|existing";
			Long memberId = 1L;

			MemberInfo memberInfo = MemberInfo.of(memberId, authSub, MemberRole.BUYER, "existing@example.com", "기존회원");
			LoginUseCase.LoginResult result = LoginUseCase.LoginResult.existingMember(memberInfo);
			given(loginUseCase.login(any(LoginUseCase.LoginCommand.class))).willReturn(result);

			// when & then
			mockMvc.perform(post("/api/v2/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"idToken\":\"" + idToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isNewUser").value(false))
				.andExpect(jsonPath("$.member.memberId").value(memberId))
				.andExpect(jsonPath("$.authSub").value(authSub));
		}

		@Test
		@DisplayName("idToken 누락 시 400 에러")
		void login_MissingIdToken_BadRequest() throws Exception {
			// when & then
			mockMvc.perform(post("/api/v2/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/v2/auth/logout")
	class LogoutTests {

		@Test
		@DisplayName("로그아웃 성공 — refreshToken 포함")
		void logout_WithRefreshToken_DelegatesToUseCase() throws Exception {
			// given
			String token = "valid.jwt.token";
			String refreshToken = "refresh_token_123";

			// when & then
			mockMvc.perform(post("/api/v2/auth/logout")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isNoContent());

			then(logoutUseCase).should().logout(
				new LogoutUseCase.LogoutCommand(token, refreshToken));
		}

		@Test
		@DisplayName("로그아웃 성공 — refreshToken 없이")
		void logout_WithoutRefreshToken_DelegatesToUseCase() throws Exception {
			// given
			String token = "valid.jwt.token";

			// when & then
			mockMvc.perform(post("/api/v2/auth/logout")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isNoContent());

			then(logoutUseCase).should().logout(
				new LogoutUseCase.LogoutCommand(token, null));
		}

		@Test
		@DisplayName("로그아웃 — body 없이 Authorization 헤더만으로도 성공")
		void logout_NoBody_DelegatesToUseCase() throws Exception {
			// given
			String token = "valid.jwt.token";

			// when & then
			mockMvc.perform(post("/api/v2/auth/logout")
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

			then(logoutUseCase).should().logout(
				new LogoutUseCase.LogoutCommand(token, null));
		}
	}
}
