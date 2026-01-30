package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.CartCreateUseCase;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MemberSignedEventListener {
    private final CartCreateUseCase cartCreateUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(MemberSignedEvent event) {
        cartCreateUseCase.createCart(event.getMemberId());
    }
}
