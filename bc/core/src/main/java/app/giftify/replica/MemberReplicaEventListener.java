package app.giftify.replica;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
public class MemberReplicaEventListener {
    private final MemberReplicaSyncUseCase memberReplicaSyncUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(MemberSignedEvent event) {
        memberReplicaSyncUseCase.syncMember(
                event.getMemberId(),
                event.getAuthSub(),
                event.getNickname()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(MemberUpdatedEvent event) {
        memberReplicaSyncUseCase.syncMember(
                event.getMemberId(),
                event.getAuthSub(),
                event.getNickname()
        );
    }
}

