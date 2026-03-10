package app.giftify.replica.member;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import app.giftify.shared.domain.event.member.SellerNicknameChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventListener {
    private final MemberSyncUseCase memberSyncUseCase;
    private final EventPublisher eventPublisher;

    // 회원가입 시 멤버 레플리카 동기화
    @ApplicationModuleListener
    public void handle(MemberSignedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
    }

    // 닉네임 수정 시 멤버 레플리카 동기화 및 ES 닉네임 업데이트 이벤트 발행 (이벤트 체이닝)
    @ApplicationModuleListener
    public void handle(MemberUpdatedEvent event) {
        syncMember(event.getMemberId(), event.getNickname());
        eventPublisher.publish(new SellerNicknameChangedEvent(event.getMemberId(), event.getNickname()));
    }

    private void syncMember(Long memberId, String nickname) {
        memberSyncUseCase.syncMember(memberId, nickname);
    }
}
