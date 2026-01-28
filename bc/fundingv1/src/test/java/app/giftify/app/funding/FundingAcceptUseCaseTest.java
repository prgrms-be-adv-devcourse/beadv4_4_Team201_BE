package app.giftify.app.funding;

import app.giftify.domain.funding.*;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundingAcceptUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private FundingAcceptUseCase fundingAcceptUseCase;

    private FundingWishlistItem createTestWishlistItem(Long receiverId) {
        return new FundingWishlistItem(
                1L,      // wishlistId
                receiverId,
                100L,    // productId
                "테스트 상품",
                50000,
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );
    }

    @Test
    @DisplayName("acceptFunding - 펀딩 수락 성공")
    void acceptFunding_success() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L; // 수령자 ID
        FundingWishlistItem item = createTestWishlistItem(memberId);
        Funding funding = Funding.startFunding(item, 50000); // 목표 달성 상태로 시작

        // startFunding으로 바로 ACHIEVED가 되지만, 명시적으로 확인
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingCompleteResponseDto result = fundingAcceptUseCase.acceptFunding(fundingId, memberId);
        
         assertThat(result.status()).isEqualTo(FundingStatus.ACCEPTED);
        
        verify(eventPublisher).publish(any(FundingAcceptedEvent.class));
    }

    @Test
    @DisplayName("acceptFunding - 펀딩을 찾을 수 없음")
    void acceptFunding_fail_notFound() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.acceptFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);
    }

    @Test
    @DisplayName("acceptFunding - 권한 없음 (수령자가 아님)")
    void acceptFunding_fail_forbidden() {
        // given
        Long fundingId = 1L;
        Long memberId = 888L; // 다른 사용자
        Long receiverId = 999L;
        FundingWishlistItem item = createTestWishlistItem(receiverId);
        Funding funding = Funding.startFunding(item, 50000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.acceptFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("acceptFunding - 달성되지 않은 펀딩은 수락 불가")
    void acceptFunding_fail_notAchieved() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L;
        FundingWishlistItem item = createTestWishlistItem(memberId);
        Funding funding = Funding.startFunding(item, 10000); // 달성 안됨

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingAcceptUseCase.acceptFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_ACHIEVED);
    }

}
