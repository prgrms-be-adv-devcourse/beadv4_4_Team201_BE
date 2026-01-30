package app.giftify.auth.application;

import app.giftify.auth.adapter.outbound.client.MemberApiClient;
import app.giftify.auth.application.inbound.LoginUseCase;
import app.giftify.shared.domain.vo.MemberInfo;
import app.giftify.support.common.event.auth.UserAuthenticatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {
    private final AuthService authService;
    private final MemberApiClient memberApiClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public LoginResult login(LoginCommand command) {
        // 1. idToken 검증 및 클레임 추출
        Jwt jwt = authService.decodeAndValidateToken(command.idToken());

        String authSub = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        String nickname = jwt.getClaimAsString("nickname");

        log.info("[Login] 토큰 검증 성공. authSub={}, email={}", authSub, email);

        // 2. 회원 조회 (Internal API 호출)
        Optional<MemberInfo> memberOpt = memberApiClient.getMemberByAuthSub(authSub);

        if (memberOpt.isPresent()) {
            log.info("[Login] 기존 회원 로그인. memberId={}", memberOpt.get().memberId());
            return LoginResult.existingMember(memberOpt.get());
        }

        // 3. 신규 사용자: UserAuthenticatedEvent 발행 → PreSignup 생성
        log.info("[Login] 신규 사용자 감지. PreSignup 이벤트 발행. authSub={}", authSub);
        eventPublisher.publishEvent(
                new UserAuthenticatedEvent(this, authSub, nickname, email, name)
        );

        return LoginResult.newUser(authSub, email, name);
    }
}
