package app.giftify.member.adapter.in.event;

import app.giftify.member.adapter.out.jpa.entity.PreSignup;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.out.PreSignupPort;
import app.giftify.support.common.event.auth.UserAuthenticatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthenticatedEventListener {
    private final RegisterMemberUseCase registerMemberUseCase;
    private final PreSignupPort preSignupPort;

    @EventListener
    public void handleUserAuthenticatedEvent(UserAuthenticatedEvent event) {
        log.info("[Event] 사용 인증 성공 이벤트 수신: {}", event.getEmail());

        if (registerMemberUseCase.existsByEmail(event.getEmail())) {
            log.info("[Event] 이미 가입된 회원입니다. 임시 정보를 생성하지 않습니다. 이메일: {}", event.getEmail());
            return;
        }

        PreSignup preSignup = new PreSignup(
                event.getAuthSub(),
                event.getEmail(),
                event.getName(),
                event.getNickname()
        );

        preSignupPort.save(preSignup);
        log.info("[Event] 임시 회원 정보(PreSignup) 저장 완료: {}", event.getEmail());
    }
}
