package app.giftify.wishlist.adapter.in.event;

import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WishlistEventListenerTest {

    @Test
    @DisplayName("WishlistEventListener 인스턴스 생성 테스트")
    void instanceCreationTest() {
        WishlistProductReplicaPort mockPort = mock(WishlistProductReplicaPort.class);

        WishlistEventListener listener = new WishlistEventListener(mockPort);

        assertThat(listener).isNotNull();
    }
}
