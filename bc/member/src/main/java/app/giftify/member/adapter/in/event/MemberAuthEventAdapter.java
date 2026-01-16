package app.giftify.member.adapter.in.event;

import app.giftify.member.application.port.in.GetMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase.RegisterCommand;
import app.giftify.support.common.event.auth.UserAuthenticatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// support:common 모듈의 인증 성공 이벤트를 수신하여 회원가입/동기화 유스케이스 트리거
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAuthEventAdapter {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final GetMemberUseCase getMemberUseCase;

    // 사용자가 인증에 성공했을 때 발행되는 이벤트 구독
    @EventListener
    public void onUserAuthenticated(UserAuthenticatedEvent event) {
        log.info("[Member Module] 인증 성공 이벤트 수신 - Email: {}", event.getEmail());

        // 이미 가입된 유저인지 확인
        if (getMemberUseCase.getMemberByAuthSub(event.getSub()).isPresent()) {
            log.info("[Member Module] 이미 가입된 유저입니다. - Sub: {}", event.getSub());
            return;
        }

        RegisterCommand command = new RegisterCommand(
                event.getEmail(),
                event.getSub(),
                event.getName(),
                null,
                null,
                null,
                event.getName()
        );

        registerMemberUseCase.registerMember(command);

        log.info("[Member Module] 회원 정보 동기화 완료 - Sub: {}", event.getSub());
    }
}
