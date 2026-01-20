package app.giftify.app.funding;

import app.giftify.app.funding.FundingGetUseCase;
import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingResponseDto;
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
class FundingGetUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @InjectMocks
    private FundingGetUseCase fundingGetUseCase;

    // ===== 테스트 헬퍼 메서드 =====

    private FundingWishlistItem createTestWishlistItem() {
        return new FundingWishlistItem(
                1L,
                1L,  // fundingReceiverId (테스트용 기본값)
                100L,
                "테스트 상품",
                50000,
                FundingWishlistItem.WishListItemStatus.IN_PROGRESS
        );
    }

    // ===== getFunding 테스트 =====

    @Test
    @DisplayName("getFunding - 진행 중인 펀딩 조회 성공")
    void getFunding_success_when_in_progress() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingResponseDto result = fundingGetUseCase.getFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fundingId()).isEqualTo(funding.getId());
        assertThat(result.targetAmount()).isEqualTo(50000);
        assertThat(result.currentAmount()).isEqualTo(10000);
        assertThat(result.status()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(result.productId()).isEqualTo(100L);
        assertThat(result.productName()).isEqualTo("테스트 상품");
        assertThat(result.productPrice()).isEqualTo(50000);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 목표 달성 펀딩 조회 성공")
    void getFunding_success_when_achieved() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000); // 첫 결제 10,000원
        
        // contribute 전 상태 확인
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.IN_PROGRESS);
        assertThat(funding.getCurrentAmount()).isEqualTo(10000);
        
        funding.contribute(40000); // 추가로 40,000원 결제 → 목표 달성
        
        // contribute 후 상태 확인 (디버깅용)
        assertThat(funding.getStatus()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(funding.getCurrentAmount()).isEqualTo(50000);

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingResponseDto result = fundingGetUseCase.getFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(FundingStatus.ACHIEVED);
        assertThat(result.currentAmount()).isEqualTo(result.targetAmount());

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 펀딩을 찾을 수 없는 경우 예외 발생")
    void getFunding_fail_when_funding_not_found() {
        // given
        Long fundingId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 종료된 펀딩은 조회 불가")
    void getFunding_fail_when_funding_closed() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        funding.close(); // 종료 처리

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 만료된 펀딩은 조회 불가")
    void getFunding_fail_when_funding_expired() {
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
        assertThatThrownBy(() -> fundingGetUseCase.getFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.NOT_IN_PROGRESS);

        verify(fundingRepository, times(1)).findById(fundingId);
    }

    @Test
    @DisplayName("getFunding - 중간 금액 펀딩 조회 검증")
    void getFunding_verify_partial_amount() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 25000); // 50% 달성

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingResponseDto result = fundingGetUseCase.getFunding(fundingId);

        // then
        assertThat(result.currentAmount()).isEqualTo(25000);
        assertThat(result.targetAmount()).isEqualTo(50000);

        verify(fundingRepository, times(1)).findById(fundingId);
    }
}

