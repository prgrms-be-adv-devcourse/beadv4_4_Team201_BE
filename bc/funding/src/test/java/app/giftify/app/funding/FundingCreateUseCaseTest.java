package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.out.funding.FundingRepository;
import app.giftify.out.funding.FundingWishlistItemRepository;
import app.giftify.shared.domain.event.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingCreateUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private FundingWishlistItemRepository fundingWishlistItemRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private FundingCreateUseCase fundingCreateUseCase;

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem() {
        return new FundingWishlistItem(
                1L,      // wishlistId
                999L,    // receiverId
                100L,    // productId
                "테스트 상품",
                50000,
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );
    }

    // ===== createFunding 테스트 =====

    @Test
    @DisplayName("createFunding - 펀딩 생성 성공")
    void createFunding_success() {
        // given
        Long itemId = 1L;
        Integer amount = 10000;
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(fundingRepository.save(any(Funding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Funding result = fundingCreateUseCase.createFunding(itemId, amount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFundingWishlistItem()).isEqualTo(item);
        assertThat(result.getCurrentAmount()).isEqualTo(10000);
        assertThat(result.getTargetAmount()).isEqualTo(50000);
        assertThat(result.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(result.getDeadline()).isNotNull();

        verify(fundingWishlistItemRepository, times(1)).findById(itemId);
        verify(fundingRepository, times(1)).save(any(Funding.class));
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("createFunding - 위시리스트 아이템을 찾을 수 없는 경우 예외 발생")
    void createFunding_fail_when_wishlist_item_not_found() {
        // given
        Long itemId = 999L;
        Integer amount = 10000;

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingCreateUseCase.createFunding(itemId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.WISHLIST_ITEM_NOT_FOUND);

        verify(fundingWishlistItemRepository, times(1)).findById(itemId);
        verify(fundingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("createFunding - 금액이 1000원 미만이면 예외 발생")
    void createFunding_fail_when_amount_less_than_1000() {
        // given
        Long itemId = 1L;
        Integer amount = 500;
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> fundingCreateUseCase.createFunding(itemId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.INVALID_AMOUNT);

        verify(fundingWishlistItemRepository, times(1)).findById(itemId);
        verify(fundingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("createFunding - 금액이 null이면 예외 발생")
    void createFunding_fail_when_amount_is_null() {
        // given
        Long itemId = 1L;
        Integer amount = null;
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // when & then
        assertThatThrownBy(() -> fundingCreateUseCase.createFunding(itemId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.INVALID_AMOUNT);

        verify(fundingWishlistItemRepository, times(1)).findById(itemId);
        verify(fundingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("createFunding - 펀딩 생성 후 FundingCreatedEvent 발행")
    void createFunding_publish_funding_created_event() {
        // given
        Long itemId = 1L;
        Integer amount = 10000;
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(fundingRepository.save(any(Funding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        fundingCreateUseCase.createFunding(itemId, amount);

        // then
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("createFunding - 초기 금액이 목표 금액과 같으면 ACHIEVED 상태")
    void createFunding_achieved_when_initial_amount_equals_target() {
        // given
        Long itemId = 1L;
        Integer amount = 50000; // 목표 금액과 동일
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(fundingRepository.save(any(Funding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Funding result = fundingCreateUseCase.createFunding(itemId, amount);

        // then
        // 첫 결제로 목표 금액 전액을 결제하면 바로 ACHIEVED 상태
        assertThat(result.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(result.getCurrentAmount()).isEqualTo(50000);
        assertThat(result.getTargetAmount()).isEqualTo(50000);
        assertThat(result.isAchieved()).isTrue();

        verify(fundingWishlistItemRepository, times(1)).findById(itemId);
        verify(fundingRepository, times(1)).save(any(Funding.class));
    }

    @Test
    @DisplayName("createFunding - 저장소 호출 순서 검증")
    void createFunding_verify_repository_call_order() {
        // given
        Long itemId = 1L;
        Integer amount = 10000;
        FundingWishlistItem item = createTestWishlistItem();

        when(fundingWishlistItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(fundingRepository.save(any(Funding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        fundingCreateUseCase.createFunding(itemId, amount);

        // then - 호출 순서: WishlistItem 조회 → Funding 저장 → Event 발행
        var inOrder = inOrder(fundingWishlistItemRepository, fundingRepository, eventPublisher);
        inOrder.verify(fundingWishlistItemRepository).findById(itemId);
        inOrder.verify(fundingRepository).save(any(Funding.class));
        inOrder.verify(eventPublisher).publish(any());
    }
}

