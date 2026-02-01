package app.giftify.wallet.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.wallet.application.inbound.CreateWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignedEventHandler {

	private final CreateWalletUseCase createWalletUseCase;

	/**
	 * ApplicationModuleListener 사용 이유:
	 * - Member 트랜잭션 커밋 후 실행 (AFTER_COMMIT)
	 * - 실패 시 Event Publication에 기록되어 재시도 가능
	 */
	@ApplicationModuleListener
	public void handle(MemberSignedEvent event) {
		log.info("[MemberSignedEventHandler] 회원가입 이벤트 수신. memberId={}, nickname={}",
			event.getMemberId(), event.getNickname());

		var result = createWalletUseCase.createIfNotExists(event.getMemberId());

		if (result.created()) {
			log.info("[MemberSignedEventHandler] 지갑 생성 완료. memberId={}, walletId={}",
				event.getMemberId(), result.walletId());
		} else {
			log.info("[MemberSignedEventHandler] 지갑이 이미 존재합니다. memberId={}, walletId={}",
				event.getMemberId(), result.walletId());
		}
	}
}
