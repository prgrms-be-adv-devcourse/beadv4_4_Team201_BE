package app.giftify.replica.member;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberEventListener {
    private final MemberSyncUseCase memberSyncUseCase;

    // 회원가입 시 멤버 레플리카 동기화
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(MemberSignedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
    }

    // 닉네임 수정 시 멤버 레플리카 동기화
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(MemberUpdatedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
    }

    private void syncMember(Long memberId, String nickname) {
        memberSyncUseCase.syncMember(memberId, nickname);
    }
}
