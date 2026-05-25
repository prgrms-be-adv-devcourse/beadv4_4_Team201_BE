package app.giftify.product.readmodel;

import app.giftify.support.common.event.EventPublisher;
import app.giftify.member.domain.event.MemberSignedEvent;
import app.giftify.member.domain.event.MemberUpdatedEvent;
import app.giftify.member.domain.event.SellerNicknameChangedEvent;
import app.giftify.support.common.security.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberViewSyncListener {

    private final MemberViewRepository memberViewRepository;
    private final EventPublisher eventPublisher;

    @ApplicationModuleListener
    public void on(MemberSignedEvent event) {
        upsert(event.getMemberId(), event.getNickname());
    }

    @ApplicationModuleListener
    public void on(MemberUpdatedEvent event) {
        upsert(event.getMemberId(), event.getNickname());
        if (event.getRole() == MemberRole.SELLER) {
            eventPublisher.publish(new SellerNicknameChangedEvent(event.getMemberId(), event.getNickname()));
        }
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
