package app.giftify.member.adapter.in.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.member.application.port.in.RegisterMemberUseCase;
import app.giftify.member.application.port.in.RegisterMemberUseCase.RegisterCommand;
import app.giftify.member.domain.member.Member;
import app.giftify.support.common.event.auth.UserAuthenticatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthenticatedEventListener {
	private final RegisterMemberUseCase registerMemberUseCase;

	@ApplicationModuleListener
	public void handleUserAuthenticatedEvent(UserAuthenticatedEvent event) {
		log.info("[UserAuthenticatedEventListener] 사용자 인증 성공 이벤트 수신: authSub={}, email={}", event.getAuthSub(), event.getEmail());

		if (registerMemberUseCase.existsByEmail(event.getEmail())) {
			log.info("[UserAuthenticatedEventListener] 이미 가입된 회원입니다. email={}", event.getEmail());
			return;
		}

		RegisterCommand command = new RegisterCommand(
			event.getEmail(),
			null,  // nickname: null이면 자동 생성
			null,
			null,
			null,
			event.getName(),
			event.getAuthSub()
		);

		Member member = registerMemberUseCase.registerMember(command);
		log.info("[UserAuthenticatedEventListener] 신규 회원 자동 생성 완료: memberId={}, email={}", member.getId(), member.getEmail());
	}
}
