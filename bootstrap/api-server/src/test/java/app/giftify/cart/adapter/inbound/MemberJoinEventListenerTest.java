package app.giftify.cart.adapter.inbound;

import app.giftify.cart.application.inbound.usecase.CartCreateUseCase;
import app.giftify.member.domain.event.MemberSignedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSignedEventListenerTest {

    @InjectMocks
    private MemberSignedEventListener memberSignedEventListener;

    @Mock
    private CartCreateUseCase cartCreateUseCase;

    @Test
    @DisplayName("회원가입 이벤트 수신 시 장바구니 생성 유스케이스가 호출되어야 한다")
    void handleMemberSignedEvent() {
        // given
        Long memberId = 1L;
        // MemberSignedEvent 생성자 시그니처에 맞춰 더미 데이터 추가
        MemberSignedEvent event = new MemberSignedEvent(memberId, "dummy-auth-sub", "dummy-nickname");

        // when
        memberSignedEventListener.handle(event);

        // then
        verify(cartCreateUseCase).createCart(memberId);
    }
}
