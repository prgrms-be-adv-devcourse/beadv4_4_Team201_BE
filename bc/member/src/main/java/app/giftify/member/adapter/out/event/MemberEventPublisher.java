package app.giftify.member.adapter.out.event;

import app.giftify.member.application.port.out.MemberEventSpringPublisher;
import app.giftify.shared.domain.event.member.MemberLoggedInEvent;
import app.giftify.shared.domain.event.member.MemberRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventPublisher implements MemberEventSpringPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishMemberRegistered(Long memberId, String email, String authSub) {
        eventPublisher.publishEvent(new MemberRegisteredEvent(memberId, email, authSub));
    }

    @Override
    public void publishMemberLoggedIn(Long memberId, String email, String authSub) {
        eventPublisher.publishEvent(new MemberLoggedInEvent(memberId, email, authSub));
    }
}
