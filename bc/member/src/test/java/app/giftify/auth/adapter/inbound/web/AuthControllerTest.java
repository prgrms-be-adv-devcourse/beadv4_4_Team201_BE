package app.giftify.auth.adapter.inbound.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import app.giftify.auth.application.AuthService;
import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;

@DisplayName("AuthController 테스트")
class AuthControllerTest {

	private MockMvc mockMvc;
	private AuthService authService;
	private LoginUseCase loginUseCase;

	@BeforeEach
	void setUp() {
		authService = org.mockito.Mockito.mock(AuthService.class);
		loginUseCase = org.mockito.Mockito.mock(LoginUseCase.class);

		mockMvc = MockMvcBuilders
			.standaloneSetup(new AuthController(authService, loginUseCase))
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

	// Note: GET /api/v2/auth/refresh는 Auth0 외부 호출이 필요하여 통합 테스트에서 검증

	@Nested
	@DisplayName("레거시 엔드포인트")
	class LegacyEndpointTests {

		@Test
		@DisplayName("GET /api/v2/auth/ - 공개 페이지 접근")
		void publicPage_Success() throws Exception {
			mockMvc.perform(get("/api/v2/auth/"))
				.andExpect(status().isOk());
		}
	}
}
