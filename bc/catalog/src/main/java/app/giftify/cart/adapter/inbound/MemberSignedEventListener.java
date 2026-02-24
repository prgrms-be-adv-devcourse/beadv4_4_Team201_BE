package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.CartCreateUseCase;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberSignedEventListener {
    private final CartCreateUseCase cartCreateUseCase;

    @ApplicationModuleListener
    public void handle(MemberSignedEvent event) {
        cartCreateUseCase.createCart(event.getMemberId());
    }
}
