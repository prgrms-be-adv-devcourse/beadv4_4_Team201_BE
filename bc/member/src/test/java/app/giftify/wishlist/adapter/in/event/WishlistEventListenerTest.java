package app.giftify.wishlist.adapter.in.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;

class WishlistEventListenerTest {

	@Test
	@DisplayName("WishlistEventListener 인스턴스 생성 테스트")
	void instanceCreationTest() {
		WishlistProductReplicaPort mockReplicaPort = mock(WishlistProductReplicaPort.class);
		WishlistItemRepositoryPort mockWishlistPort = mock(WishlistItemRepositoryPort.class);

		WishlistEventListener listener = new WishlistEventListener(mockReplicaPort, mockWishlistPort);

		assertThat(listener).isNotNull();
	}
}
