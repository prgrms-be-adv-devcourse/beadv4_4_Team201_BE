package app.giftify.wishlist.adapter.in.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistEventListenerTest {

    @Test
    @DisplayName("WishlistEventListener 인스턴스 생성 테스트")
    void instanceCreationTest() {
        WishlistEventListener listener = new WishlistEventListener();
        assertThat(listener).isNotNull();
    }
}
