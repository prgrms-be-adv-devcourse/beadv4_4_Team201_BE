package app.giftify.in;

import static org.springframework.transaction.annotation.Propagation.*;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.app.FundingMemberSyncUseCase;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FundingMemberEventListener {
	private final FundingMemberSyncUseCase fundingMemberSyncUseCase;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = REQUIRES_NEW)
	public void handle(MemberSignedEvent event) {
		fundingMemberSyncUseCase.syncMember(
			event.getMemberId(),
			event.getAuthSub(),
			event.getNickname()
		);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = REQUIRES_NEW)
	public void handle(MemberUpdatedEvent event) {
		fundingMemberSyncUseCase.syncMember(
			event.getMemberId(),
			event.getAuthSub(),
			event.getNickname()
		);
	}
}
