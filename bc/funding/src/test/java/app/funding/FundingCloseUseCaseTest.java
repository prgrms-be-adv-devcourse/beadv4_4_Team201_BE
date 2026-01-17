package app.funding;

import app.giftify.app.funding.FundingCloseUseCase;
import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.FundingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingCloseUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @InjectMocks
    private FundingCloseUseCase fundingCloseUseCase;

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem() {
        return new FundingWishlistItem(
                1L,
                100L,
                "테스트 상품",
                50000,
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );
    }

    // ===== closeFunding 테스트 =====

    @Test
    @DisplayName("closeFunding - 진행 중인 펀딩 강제 종료 성공")
    void closeFunding_success_when_in_progress() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingCompleteResponseDto result = fundingCloseUseCase.closeFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fundingId()).isEqualTo(funding.getId());
        assertThat(result.status()).isEqualTo(FundingStatus.CLOSED);
        assertThat(result.closeAt()).isNotNull();

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("closeFunding - 목표 달성 펀딩도 종료 가능")
    void closeFunding_success_when_achieved() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        funding.contribute(40000); // 추가로 40,000원 → 목표 달성

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingCompleteResponseDto result = fundingCloseUseCase.closeFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(FundingStatus.CLOSED);
        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("closeFunding - 펀딩을 찾을 수 없는 경우 예외 발생")
    void closeFunding_fail_when_funding_not_found() {
        // given
        Long fundingId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("closeFunding - 이미 종료된 펀딩은 재종료 불가")
    void closeFunding_fail_when_already_closed() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        funding.close(); // 먼저 종료

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.ALREADY_TERMINATED);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("closeFunding - 이미 만료된 펀딩은 종료 불가")
    void closeFunding_fail_when_already_expired() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);

        // endAt을 과거로 설정 (리플렉션)
        try {
            java.lang.reflect.Field endAtField = Funding.class.getDeclaredField("endAt");
            endAtField.setAccessible(true);
            endAtField.set(funding, java.time.LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        funding.expire(); // 만료 처리

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingCloseUseCase.closeFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.ALREADY_TERMINATED);

        verify(fundingRepository, times(1)).findById(fundingId);
    }
}

