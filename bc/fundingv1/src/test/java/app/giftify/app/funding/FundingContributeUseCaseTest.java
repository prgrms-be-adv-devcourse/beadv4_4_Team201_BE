package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
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
class FundingContributeUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private FundingContributeUseCase fundingContributeUseCase;

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

    // ===== contribute 테스트 =====

    @Test
    @DisplayName("contribute - 펀딩 참여 성공")
    void contribute_success() {
        // given
        Long fundingId = 1L;
        Integer amount = 10000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        fundingContributeUseCase.contribute(fundingId, amount);

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(30000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 펀딩이 존재하지 않으면 예외 발생")
    void contribute_fail_when_funding_not_found() {
        // given
        Long fundingId = 999L;
        Integer amount = 10000;

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 목표 달성 시 FundingAchievedEvent 발행")
    void contribute_publish_achieved_event_when_target_reached() {
        // given
        Long fundingId = 1L;
        Integer amount = 30000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000); // 현재 20000원, 목표 50000원

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        fundingContributeUseCase.contribute(fundingId, amount); // 30000원 추가 → 총 50000원 달성

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(funding.isAchieved()).isTrue();

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, times(1)).publish(any(FundingAchievedEvent.class));
    }

    @Test
    @DisplayName("contribute - 목표 미달성 시 이벤트 발행 안 함")
    void contribute_not_publish_event_when_target_not_reached() {
        // given
        Long fundingId = 1L;
        Integer amount = 5000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000); // 현재 20000원

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        fundingContributeUseCase.contribute(fundingId, amount); // 5000원 추가 → 총 25000원 (미달성)

        // then
        assertThat(funding.getCurrentAmount()).isEqualTo(25000);
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(funding.isAchieved()).isFalse();

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 1000원 미만 금액으로 참여 시 예외")
    void contribute_fail_when_amount_less_than_1000() {
        // given
        Long fundingId = 1L;
        Integer amount = 500;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.INVALID_AMOUNT);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 잔여 금액 초과 시 예외")
    void contribute_fail_when_exceed_remaining_amount() {
        // given
        Long fundingId = 1L;
        Integer amount = 40000; // 잔여 금액은 30000원인데 40000원 시도
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000); // 현재 20000원, 잔여 30000원

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.EXCEED_REMAINING_AMOUNT);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 이미 달성된 펀딩에 참여 시 예외")
    void contribute_fail_when_already_achieved() {
        // given
        Long fundingId = 1L;
        Integer amount = 1000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 50000); // 바로 달성

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - 종료된 펀딩에 참여 시 예외")
    void contribute_fail_when_closed() {
        // given
        Long fundingId = 1L;
        Integer amount = 10000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000);
        funding.close(); // 강제 종료

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingContributeUseCase.contribute(fundingId, amount))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("contribute - FundingAchievedEvent에 올바른 데이터 포함")
    void contribute_achieved_event_contains_correct_data() {
        // given
        Long fundingId = 1L;
        Integer amount = 30000;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 20000);
        
        // Funding ID 설정 (리플렉션)
        try {
            java.lang.reflect.Field idField = funding.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(funding, fundingId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        fundingContributeUseCase.contribute(fundingId, amount);

        // then
        verify(eventPublisher, times(1)).publish(argThat(event -> {
            if (!(event instanceof FundingAchievedEvent)) return false;
            FundingAchievedEvent achievedEvent = (FundingAchievedEvent) event;
            return achievedEvent.getFundingId().equals(fundingId) &&
                   achievedEvent.getWishlistItemId().equals(1L) &&
                   achievedEvent.getAchievedAmount().equals(50000) &&
                   achievedEvent.getProductId().equals(100L) &&
                   achievedEvent.getFundingReceiverId().equals(999L);
        }));
    }
}

