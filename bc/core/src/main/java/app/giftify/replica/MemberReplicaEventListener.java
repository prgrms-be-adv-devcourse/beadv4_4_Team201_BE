package app.giftify.replica;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberReplicaEventListener {
    private final MemberReplicaSyncUseCase memberReplicaSyncUseCase;

    @ApplicationModuleListener
    public void handle(MemberSignedEvent event) {
        memberReplicaSyncUseCase.syncMember(
                event.getMemberId(),
                event.getNickname()
        );
    }

    @ApplicationModuleListener
    public void handle(MemberUpdatedEvent event) {
        memberReplicaSyncUseCase.syncMember(
                event.getMemberId(),
                event.getNickname()
        );
    }
}

