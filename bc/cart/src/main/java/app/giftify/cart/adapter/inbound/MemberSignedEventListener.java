package app.giftify.cart.adapter.inbound;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.cart.application.inbound.usecase.CartCreateUseCase;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.support.common.annotation.EventIdempotent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSignedEventListener {
	private final CartCreateUseCase cartCreateUseCase;

	@ApplicationModuleListener
	@EventIdempotent(prefix = "CART_CREATE")
	public void handle(MemberSignedEvent event) {
		cartCreateUseCase.createCart(event.getMemberId());
	}
}
