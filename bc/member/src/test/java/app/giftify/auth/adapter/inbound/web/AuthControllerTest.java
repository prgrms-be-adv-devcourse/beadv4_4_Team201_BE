package app.giftify.auth.adapter.inbound.web;

import app.giftify.auth.application.TokenBlacklistService;
import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController 테스트")
class AuthControllerTest {

    private MockMvc mockMvc;
    private LoginUseCase loginUseCase;
    private TokenBlacklistService tokenBlacklistService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        loginUseCase = org.mockito.Mockito.mock(LoginUseCase.class);
        tokenBlacklistService = org.mockito.Mockito.mock(TokenBlacklistService.class);
        jwtDecoder = org.mockito.Mockito.mock(JwtDecoder.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(loginUseCase, tokenBlacklistService, jwtDecoder))
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
        @DisplayName("로그아웃 성공 - 토큰 블랙리스트 등록")
        void logout_Success() throws Exception {
            // given
            String token = "valid.jwt.token";
            String jti = "token-id-123";
            Instant expiresAt = Instant.now().plusSeconds(3600);

            Jwt jwt = Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .claim("sub", "auth0|user1")
                    .jti(jti)
                    .expiresAt(expiresAt)
                    .issuedAt(Instant.now())
                    .build();

            given(jwtDecoder.decode(token)).willReturn(jwt);

            // when & then
            mockMvc.perform(post("/api/v2/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            verify(tokenBlacklistService).revokeToken(eq(jti), any(Duration.class), eq("logout"));
        }
    }
}
