package app.giftify.app.funding;

import app.giftify.domain.funding.*;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingRefuseUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private FundingRefuseUseCase fundingRefuseUseCase;

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

    private void setFundingStatus(Funding funding, FundingStatus status) {
        try {
            Field statusField = Funding.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(funding, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("refuseFunding - 펀딩 거절 성공")
    void refuseFunding_success() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L; // 수령자 ID
        FundingWishlistItem item = createTestWishlistItem(memberId);
        Funding funding = Funding.startFunding(item, 50000); // 목표 달성 상태로 시작

        // startFunding으로 바로 ACHIEVED가 되지만, 명시적으로 확인
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingCompleteResponseDto result = fundingRefuseUseCase.refuseFunding(fundingId, memberId);

        // then
        assertThat(result.status()).isEqualTo(FundingStatus.REFUSED);
        verify(eventPublisher).publish(any(FundingCanceledEvent.class));
    }

    @Test
    @DisplayName("refuseFunding - 펀딩을 찾을 수 없음")
    void refuseFunding_fail_notFound() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingRefuseUseCase.refuseFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);
    }

    @Test
    @DisplayName("refuseFunding - 권한 없음 (수령자가 아님)")
    void refuseFunding_fail_forbidden() {
        // given
        Long fundingId = 1L;
        Long memberId = 888L; // 다른 사용자
        Long receiverId = 999L;
        FundingWishlistItem item = createTestWishlistItem(receiverId);
        Funding funding = Funding.startFunding(item, 50000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingRefuseUseCase.refuseFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("refuseFunding - 달성되지 않은 펀딩은 거절 불가")
    void refuseFunding_fail_notAchieved() {
        // given
        Long fundingId = 1L;
        Long memberId = 999L;
        FundingWishlistItem item = createTestWishlistItem(memberId);
        Funding funding = Funding.startFunding(item, 10000); // 달성 안됨 (IN_PROGRESS)

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingRefuseUseCase.refuseFunding(fundingId, memberId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_ACHIEVED);
    }
}
