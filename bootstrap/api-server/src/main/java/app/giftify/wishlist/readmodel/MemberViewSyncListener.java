package app.giftify.wishlist.readmodel;

import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.event.member.MemberUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberViewSyncListener {

    private final MemberViewRepository memberViewRepository;

    @ApplicationModuleListener
    public void on(MemberSignedEvent event) {
        upsert(event.getMemberId(), event.getNickname());
    }

    @ApplicationModuleListener
    public void on(MemberUpdatedEvent event) {
        upsert(event.getMemberId(), event.getNickname());
    }

    @Transactional
    public void upsert(Long memberId, String nickname) {
        memberViewRepository.findById(memberId)
                .ifPresentOrElse(
                        existing -> existing.updateNickname(nickname),
                        () -> memberViewRepository.save(new MemberView(memberId, nickname))
                );
    }
}
