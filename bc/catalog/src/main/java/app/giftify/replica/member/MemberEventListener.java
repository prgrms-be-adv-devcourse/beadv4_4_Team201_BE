package app.giftify.replica.member;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventListener {
    private final MemberSyncUseCase memberSyncUseCase;

    // 회원가입 시 멤버 레플리카 동기화
    @ApplicationModuleListener
    public void handle(MemberSignedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
    }

    // 닉네임 수정 시 멤버 레플리카 동기화
    @ApplicationModuleListener
    public void handle(MemberUpdatedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
    }

    private void syncMember(Long memberId, String nickname) {
        memberSyncUseCase.syncMember(memberId, nickname);
    }
}
