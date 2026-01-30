package app.giftify.auth.application;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.auth.application.inbound.LoginUseCase.LoginCommand;
import app.giftify.auth.application.inbound.LoginUseCase.LoginResult;
import app.giftify.shared.domain.type.MemberRole;
import app.giftify.shared.domain.vo.MemberInfo;
import app.giftify.support.common.event.auth.UserAuthenticatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private MemberApiClient memberApiClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LoginService loginService;

    @Nested
    @DisplayName("Given 유효한 idToken으로 로그인 요청")
    class Given_ValidIdToken {

        @Nested
        @DisplayName("When 기존 회원인 경우")
        class When_ExistingMember {

            @Test
            @DisplayName("Then isNewUser=false와 회원 정보 반환")
            void Then_ReturnsExistingMemberInfo() {
                // given
                String idToken = "valid.id.token";
                String authSub = "auth0|12345";
                String email = "test@example.com";
                String name = "테스터";

                Jwt jwt = createMockJwt(authSub, email, name, "testNick");
                given(authService.decodeAndValidateToken(idToken)).willReturn(jwt);

                MemberInfo memberInfo = MemberInfo.of(1L, authSub, MemberRole.BUYER, email, "기존닉네임");
                given(memberApiClient.getMemberByAuthSub(authSub)).willReturn(Optional.of(memberInfo));

                // when
                LoginResult result = loginService.login(new LoginCommand(idToken));

                // then
                assertThat(result.isNewUser()).isFalse();
                assertThat(result.member()).isPresent();
                assertThat(result.member().get().memberId()).isEqualTo(1L);
                assertThat(result.authSub()).isEqualTo(authSub);

                verify(eventPublisher, never()).publishEvent(any(UserAuthenticatedEvent.class));
            }
        }

        @Nested
        @DisplayName("When 신규 사용자인 경우")
        class When_NewUser {

            @Test
            @DisplayName("Then isNewUser=true 반환 및 UserAuthenticatedEvent 발행")
            void Then_ReturnsNewUserAndPublishesEvent() {
                // given
                String idToken = "valid.id.token";
                String authSub = "auth0|newuser";
                String email = "newuser@example.com";
                String name = "신규유저";
                String nickname = "newNick";

                Jwt jwt = createMockJwt(authSub, email, name, nickname);
                given(authService.decodeAndValidateToken(idToken)).willReturn(jwt);
                given(memberApiClient.getMemberByAuthSub(authSub)).willReturn(Optional.empty());

                // when
                LoginResult result = loginService.login(new LoginCommand(idToken));

                // then
                assertThat(result.isNewUser()).isTrue();
                assertThat(result.member()).isEmpty();
                assertThat(result.authSub()).isEqualTo(authSub);
                assertThat(result.email()).isEqualTo(email);
                assertThat(result.nickname()).isEqualTo(name);

                ArgumentCaptor<UserAuthenticatedEvent> eventCaptor =
                        ArgumentCaptor.forClass(UserAuthenticatedEvent.class);
                verify(eventPublisher).publishEvent(eventCaptor.capture());

                UserAuthenticatedEvent event = eventCaptor.getValue();
                assertThat(event.getAuthSub()).isEqualTo(authSub);
                assertThat(event.getEmail()).isEqualTo(email);
                assertThat(event.getName()).isEqualTo(name);
            }
        }
    }

    private Jwt createMockJwt(String subject, String email, String name, String nickname) {
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", subject,
                        "email", email,
                        "name", name,
                        "nickname", nickname
                )
        );
    }
}
