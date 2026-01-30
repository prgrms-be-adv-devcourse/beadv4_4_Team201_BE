package app.giftify.wishlist.adapter.in.event;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;

@ExtendWith(MockitoExtension.class)
class WishlistEventListenerTest {

	@Mock
	private WishlistProductReplicaPort wishlistProductReplicaPort;

	@Mock
	private WishlistItemRepositoryPort wishlistItemRepositoryPort;

	@Mock
	private WishlistRepositoryPort wishlistRepositoryPort;

	private WishlistEventListener listener;

	@BeforeEach
	void setUp() {
		listener = new WishlistEventListener(
			wishlistProductReplicaPort,
			wishlistItemRepositoryPort,
			wishlistRepositoryPort
		);
	}

	@Test
	@DisplayName("WishlistEventListener 인스턴스 생성 테스트")
	void instanceCreationTest() {
		assertThat(listener).isNotNull();
	}

	@Nested
	@DisplayName("회원 이벤트")
	class MemberEventTests {

		@Test
		@DisplayName("회원 가입 시 위시리스트가 자동 생성된다")
		void handleMemberSigned_CreatesWishlist() {
			// given
			Long memberId = 1L;
			MemberSignedEvent event = new MemberSignedEvent(memberId, "auth-sub-123", "testUser");

			// when
			listener.handleMemberSigned(event);

			// then
			ArgumentCaptor<Wishlist> wishlistCaptor = ArgumentCaptor.forClass(Wishlist.class);
			verify(wishlistRepositoryPort).save(wishlistCaptor.capture());

			Wishlist savedWishlist = wishlistCaptor.getValue();
			assertThat(savedWishlist.getMemberId()).isEqualTo(memberId);
			assertThat(savedWishlist.getVisibility()).isEqualTo(Visibility.PUBLIC);
		}
	}

	@Nested
	@DisplayName("상품 판매 상태 이벤트")
	class ProductSaleEventTests {

		//     @Test
		//     @DisplayName("상품 판매 가능 이벤트 - 기존 레플리카가 있으면 판매 상태를 활성화한다")
		//     void handleProductSaleEnabled_ExistingReplica_EnablesSale() {
		//         // given
		//         Long productId = 1L;
		//         ProductSaleEnabledEvent event = new ProductSaleEnabledEvent(LocalDateTime.now(), productId);
		//
		//         WishlistProductReplica replica = WishlistProductReplica.builder()
		//                 .productId(productId)
		//                 .wishlistAllowed(false)
		//                 .build();
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));
		//
		//         // when
		//         listener.handleProductSaleEnabled(event);
		//
		//         // then
		//         verify(wishlistProductReplicaPort).upsert(replica);
		//         assertThat(replica.isWishlistAllowed()).isTrue();
		//     }
		//
		//     @Test
		//     @DisplayName("상품 판매 가능 이벤트 - 레플리카가 없으면 새로 생성한다")
		//     void handleProductSaleEnabled_NoReplica_CreatesNew() {
		//         // given
		//         Long productId = 1L;
		//         ProductSaleEnabledEvent event = new ProductSaleEnabledEvent(LocalDateTime.now(), productId);
		//
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());
		//
		//         // when
		//         listener.handleProductSaleEnabled(event);
		//
		//         // then
		//         ArgumentCaptor<WishlistProductReplica> captor = ArgumentCaptor.forClass(WishlistProductReplica.class);
		//         verify(wishlistProductReplicaPort).upsert(captor.capture());
		//
		//         WishlistProductReplica saved = captor.getValue();
		//         assertThat(saved.getProductId()).isEqualTo(productId);
		//         assertThat(saved.isWishlistAllowed()).isTrue();
		//     }
		//
		//     @Test
		//     @DisplayName("상품 판매 불가 이벤트 - 기존 레플리카가 있으면 판매 상태를 비활성화한다")
		//     void handleProductSaleDisabled_ExistingReplica_DisablesSale() {
		//         // given
		//         Long productId = 1L;
		//         ProductSaleDisabledEvent event = new ProductSaleDisabledEvent(LocalDateTime.now(), productId);
		//
		//         WishlistProductReplica replica = WishlistProductReplica.builder()
		//                 .productId(productId)
		//                 .wishlistAllowed(true)
		//                 .build();
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));
		//
		//         // when
		//         listener.handleProductSaleDisabled(event);
		//
		//         // then
		//         verify(wishlistProductReplicaPort).upsert(replica);
		//         assertThat(replica.isWishlistAllowed()).isFalse();
		//     }
		//
		//     @Test
		//     @DisplayName("상품 판매 불가 이벤트 - 레플리카가 없으면 새로 생성한다")
		//     void handleProductSaleDisabled_NoReplica_CreatesNew() {
		//         // given
		//         Long productId = 1L;
		//         ProductSaleDisabledEvent event = new ProductSaleDisabledEvent(LocalDateTime.now(), productId);
		//
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());
		//
		//         // when
		//         listener.handleProductSaleDisabled(event);
		//
		//         // then
		//         ArgumentCaptor<WishlistProductReplica> captor = ArgumentCaptor.forClass(WishlistProductReplica.class);
		//         verify(wishlistProductReplicaPort).upsert(captor.capture());
		//
		//         WishlistProductReplica saved = captor.getValue();
		//         assertThat(saved.getProductId()).isEqualTo(productId);
		//         assertThat(saved.isWishlistAllowed()).isFalse();
		//     }
		// }
		//
		// @Nested
		// @DisplayName("상품 레플리카 이벤트")
		// class ProductReplicaEventTests {
		//
		//     @Test
		//     @DisplayName("상품 등록 이벤트 - 기존 레플리카가 있으면 정보를 업데이트한다")
		//     void handleProductReplicaCreationRequested_ExistingReplica_Updates() {
		//         // given
		//         Long productId = 1L;
		//         ProductReplicaCreationRequestedEvent event = new ProductReplicaCreationRequestedEvent(
		//                 LocalDateTime.now(), productId, "New Product", 15000
		//         );
		//
		//         WishlistProductReplica replica = WishlistProductReplica.builder()
		//                 .productId(productId)
		//                 .name("Old Product")
		//                 .price(10000)
		//                 .sellerNickname("OldSeller")
		//                 .wishlistAllowed(true)
		//                 .build();
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));
		//
		//         // when
		//         listener.handleProductReplicaCreationRequested(event);
		//
		//         // then
		//         verify(wishlistProductReplicaPort).upsert(replica);
		//         assertThat(replica.getName()).isEqualTo("New Product");
		//         assertThat(replica.getPrice()).isEqualTo(15000);
		//         assertThat(replica.getSellerNickname()).isEqualTo("NewSeller");
		//     }
		//
		//     @Test
		//     @DisplayName("상품 등록 이벤트 - 레플리카가 없으면 새로 생성한다")
		//     void handleProductReplicaCreationRequested_NoReplica_CreatesNew() {
		//         // given
		//         Long productId = 1L;
		//         ProductReplicaCreationRequestedEvent event = new ProductReplicaCreationRequestedEvent(
		//                 LocalDateTime.now(), productId, "New Product", 15000
		//         );
		//
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());
		//
		//         // when
		//         listener.handleProductReplicaCreationRequested(event);
		//
		//         // then
		//         ArgumentCaptor<WishlistProductReplica> captor = ArgumentCaptor.forClass(WishlistProductReplica.class);
		//         verify(wishlistProductReplicaPort).upsert(captor.capture());
		//
		//         WishlistProductReplica saved = captor.getValue();
		//         assertThat(saved.getProductId()).isEqualTo(productId);
		//         assertThat(saved.getName()).isEqualTo("New Product");
		//         assertThat(saved.getPrice()).isEqualTo(15000);
		//         assertThat(saved.getSellerNickname()).isEqualTo("NewSeller");
		//         assertThat(saved.isWishlistAllowed()).isFalse();
		//     }
		//
		//     @Test
		//     @DisplayName("상품 정보 변경 이벤트 - 기존 레플리카가 있으면 정보를 업데이트한다")
		//     void handleProductReplicaUpdated_ExistingReplica_Updates() {
		//         // given
		//         Long productId = 1L;
		//         ProductReplicaUpdatedEvent event = new ProductReplicaUpdatedEvent(
		//                 LocalDateTime.now(), productId, "Updated Product", 20000
		//         );
		//
		//         WishlistProductReplica replica = WishlistProductReplica.builder()
		//                 .productId(productId)
		//                 .name("Old Product")
		//                 .price(10000)
		//                 .sellerNickname("OldSeller")
		//                 .wishlistAllowed(true)
		//                 .build();
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.of(replica));
		//
		//         // when
		//         listener.handleProductReplicaUpdated(event);
		//
		//         // then
		//         verify(wishlistProductReplicaPort).upsert(replica);
		//         assertThat(replica.getName()).isEqualTo("Updated Product");
		//         assertThat(replica.getPrice()).isEqualTo(20000);
		//         assertThat(replica.getSellerNickname()).isEqualTo("UpdatedSeller");
		//     }
		//
		//     @Test
		//     @DisplayName("상품 정보 변경 이벤트 - 레플리카가 없으면 새로 생성한다")
		//     void handleProductReplicaUpdated_NoReplica_CreatesNew() {
		//         // given
		//         Long productId = 1L;
		//         ProductReplicaUpdatedEvent event = new ProductReplicaUpdatedEvent(
		//                 LocalDateTime.now(), productId, "New Product", 15000
		//         );
		//
		//         given(wishlistProductReplicaPort.findByProductId(productId)).willReturn(Optional.empty());
		//
		//         // when
		//         listener.handleProductReplicaUpdated(event);
		//
		//         // then
		//         ArgumentCaptor<WishlistProductReplica> captor = ArgumentCaptor.forClass(WishlistProductReplica.class);
		//         verify(wishlistProductReplicaPort).upsert(captor.capture());
		//
		//         WishlistProductReplica saved = captor.getValue();
		//         assertThat(saved.getProductId()).isEqualTo(productId);
		//         assertThat(saved.getName()).isEqualTo("New Product");
		//         assertThat(saved.getPrice()).isEqualTo(15000);
		//         assertThat(saved.getSellerNickname()).isEqualTo("NewSeller");
		//         assertThat(saved.isWishlistAllowed()).isFalse();
		//     }
		// }

		@Nested
		@DisplayName("펀딩 이벤트")
		class FundingEventTests {

			@Test
			@DisplayName("펀딩 생성 이벤트 - 위시리스트 아이템 상태가 IN_PROGRESS로 변경된다")
			void handleFundingCreated_ChangesStatusToInProgress() {
				// given
				Long wishlistItemId = 1L;
				FundingCreatedEvent event = new FundingCreatedEvent(
					100L, wishlistItemId, 50000, LocalDateTime.now().plusDays(7)
				);

				WishlistItem wishlistItem = WishlistItem.builder()
					.id(wishlistItemId)
					.wishlistId(10L)
					.productId(1L)
					.wishlistItemStatus(WishlistItemStatus.PENDING)
					.build();
				given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

				// when
				listener.handleFundingCreated(event);

				// then
				assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.IN_PROGRESS);
			}

			@Test
			@DisplayName("펀딩 달성 이벤트 - 위시리스트 아이템 상태가 REQUESTED_CONFIRM으로 변경된다")
			void handleFundingAchieved_ChangesStatusToRequestedConfirm() {
				// given
				Long wishlistItemId = 1L;
				FundingAchievedEvent event = new FundingAchievedEvent(
					100L, wishlistItemId, 50000, 1L, 1L
				);

				WishlistItem wishlistItem = WishlistItem.builder()
					.id(wishlistItemId)
					.wishlistId(10L)
					.productId(1L)
					.wishlistItemStatus(WishlistItemStatus.IN_PROGRESS)
					.build();
				given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

				// when
				listener.handleFundingAchieved(event);

				// then
				assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.REQUESTED_CONFIRM);
			}

			@Test
			@DisplayName("펀딩 수락 이벤트 - 위시리스트 아이템 상태가 COMPLETED로 변경된다")
			void handleFundingAccepted_ChangesStatusToCompleted() {
				// given
				Long wishlistItemId = 1L;
				FundingAcceptedEvent event = new FundingAcceptedEvent(100L, wishlistItemId);

				WishlistItem wishlistItem = WishlistItem.builder()
					.id(wishlistItemId)
					.wishlistId(10L)
					.productId(1L)
					.wishlistItemStatus(WishlistItemStatus.REQUESTED_CONFIRM)
					.build();
				given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

				// when
				listener.handleFundingAccepted(event);

				// then
				assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.COMPLETED);
			}

			@Test
			@DisplayName("펀딩 거절 이벤트 - 위시리스트 아이템 상태가 NO_THANKS로 변경된다")
			void handleFundingCanceled_ChangesStatusToNoThanks() {
				// given
				Long wishlistItemId = 1L;
				FundingCanceledEvent event = new FundingCanceledEvent(
					100L, wishlistItemId, 50000, 1L, 1L
				);

				WishlistItem wishlistItem = WishlistItem.builder()
					.id(wishlistItemId)
					.wishlistId(10L)
					.productId(1L)
					.wishlistItemStatus(WishlistItemStatus.REQUESTED_CONFIRM)
					.build();
				given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

				// when
				listener.handleFundingCanceled(event);

				// then
				assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.NO_THANKS);
			}

			@Test
			@DisplayName("펀딩 만료 이벤트 - 위시리스트 아이템 상태가 PENDING으로 변경된다")
			void handleFundingExpired_ChangesStatusToPending() {
				// given
				Long wishlistItemId = 1L;
				FundingExpiredEvent event = new FundingExpiredEvent(
					100L, wishlistItemId, 30000, 1L
				);

				WishlistItem wishlistItem = WishlistItem.builder()
					.id(wishlistItemId)
					.wishlistId(10L)
					.productId(1L)
					.wishlistItemStatus(WishlistItemStatus.REQUESTED_CONFIRM)
					.build();
				given(wishlistItemRepositoryPort.findById(wishlistItemId)).willReturn(Optional.of(wishlistItem));

				// when
				listener.handleFundingExpired(event);

				// then
				assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.PENDING);
			}
		}
	}
}
