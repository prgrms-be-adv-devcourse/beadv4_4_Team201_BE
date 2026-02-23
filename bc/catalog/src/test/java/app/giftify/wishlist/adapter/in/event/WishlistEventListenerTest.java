package app.giftify.wishlist.adapter.in.event;

import app.giftify.shared.domain.event.funding.*;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.vo.FundingDetail;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistEventListenerTest {

    @Mock
    private WishlistItemRepositoryPort wishlistItemRepositoryPort;

    @Mock
    private WishlistRepositoryPort wishlistRepositoryPort;

    private WishlistEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new WishlistEventListener(
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
    @DisplayName("펀딩 이벤트")
    class FundingEventTests {

        @Test
        @DisplayName("복수 펀딩 생성 이벤트 - 모든 위시리스트 아이템 상태가 IN_PROGRESS로 변경된다")
        void handleFundingListCreated_ChangesAllStatusesToInProgress() {
            // given
            Long wishlistItemId1 = 1L;
            Long wishlistItemId2 = 2L;
            Long wishlistItemId3 = 3L;

            List<FundingDetail> fundingDetails = List.of(
                    new FundingDetail(100L, 10L, wishlistItemId1),
                    new FundingDetail(101L, 11L, wishlistItemId2),
                    new FundingDetail(102L, 12L, wishlistItemId3)
            );
            FundingCreatedEventV2 event = new FundingCreatedEventV2(fundingDetails);

            WishlistItem item1 = WishlistItem.builder()
                    .id(wishlistItemId1).wishlistId(10L).productId(1L)
                    .wishlistItemStatus(WishlistItemStatus.PENDING).build();
            WishlistItem item2 = WishlistItem.builder()
                    .id(wishlistItemId2).wishlistId(10L).productId(2L)
                    .wishlistItemStatus(WishlistItemStatus.PENDING).build();
            WishlistItem item3 = WishlistItem.builder()
                    .id(wishlistItemId3).wishlistId(10L).productId(3L)
                    .wishlistItemStatus(WishlistItemStatus.PENDING).build();

            given(wishlistItemRepositoryPort.findById(wishlistItemId1)).willReturn(Optional.of(item1));
            given(wishlistItemRepositoryPort.findById(wishlistItemId2)).willReturn(Optional.of(item2));
            given(wishlistItemRepositoryPort.findById(wishlistItemId3)).willReturn(Optional.of(item3));

            // when
            listener.handleFundingListCreated(event);

            // then
            assertThat(item1.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.IN_PROGRESS);
            assertThat(item2.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.IN_PROGRESS);
            assertThat(item3.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.IN_PROGRESS);
            verify(wishlistItemRepositoryPort).save(item1);
            verify(wishlistItemRepositoryPort).save(item2);
            verify(wishlistItemRepositoryPort).save(item3);
        }

        @Test
        @DisplayName("펀딩 달성 이벤트 - 위시리스트 아이템 상태가 REQUESTED_CONFIRM으로 변경된다")
        void handleFundingAchieved_ChangesStatusToRequestedConfirm() {
            // given
            Long wishlistItemId = 1L;
            FundingAchievedEvent event = new FundingAchievedEvent(
                    100L, wishlistItemId
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
            Long productId = 1L;
            Long receiverId = 1L;
            FundingAcceptedEvent event = new FundingAcceptedEvent(100L, wishlistItemId, productId, receiverId, LocalDateTime.now());

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
        @DisplayName("펀딩 거절 이벤트 - 위시리스트 아이템 상태가 PENDING 으로 변경된다")
        void handleFundingCanceled_ChangesStatusToNoThanks() {
            // given
            Long wishlistItemId = 1L;
            FundingCanceledEvent event = new FundingCanceledEvent(
                    100L, wishlistItemId, 50000
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
            assertThat(wishlistItem.getWishlistItemStatus()).isEqualTo(WishlistItemStatus.PENDING);
        }

        @Test
        @DisplayName("펀딩 만료 이벤트 - 위시리스트 아이템 상태가 PENDING으로 변경된다")
        void handleFundingExpired_ChangesStatusToPending() {
            // given
            Long wishlistItemId = 1L;
            FundingExpiredEvent event = new FundingExpiredEvent(
                    100L, wishlistItemId, 30000
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
