package app.funding;

import app.giftify.app.funding.FundingExpireUseCase;
import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingStatus;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundingExpireUseCaseTest {

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private FundingExpireUseCase fundingExpireUseCase;

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

    private void setEndAtToPast(Funding funding) {
        try {
            java.lang.reflect.Field endAtField = Funding.class.getDeclaredField("endAt");
            endAtField.setAccessible(true);
            endAtField.set(funding, LocalDateTime.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ===== expireFunding (단일 만료) 테스트 =====

    @Test
    @DisplayName("expireFunding - 기한 지난 진행 중 펀딩 만료 성공")
    void expireFunding_success_when_expired_date() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        setEndAtToPast(funding); // endAt을 과거로 설정

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when
        FundingCompleteResponseDto result = fundingExpireUseCase.expireFunding(fundingId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fundingId()).isEqualTo(funding.getId());
        assertThat(result.status()).isEqualTo(FundingStatus.EXPIRED);
        assertThat(result.closeAt()).isNotNull();

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, times(1)).publish(any());
    }

    @Test
    @DisplayName("expireFunding - 펀딩을 찾을 수 없는 경우 예외 발생")
    void expireFunding_fail_when_funding_not_found() {
        // given
        Long fundingId = 999L;
        when(fundingRepository.findById(fundingId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fundingExpireUseCase.expireFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.FUNDING_NOT_FOUND);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("expireFunding - 아직 기한이 지나지 않은 펀딩은 만료 불가")
    void expireFunding_fail_when_not_expired_yet() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000); // endAt은 15일 후

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingExpireUseCase.expireFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.IS_NOT_EXPIRED);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("expireFunding - 이미 종료된 펀딩은 만료 불가")
    void expireFunding_fail_when_already_closed() {
        // given
        Long fundingId = 1L;
        FundingWishlistItem item = createTestWishlistItem();
        Funding funding = Funding.startFunding(item, 10000);
        funding.close(); // 먼저 종료

        when(fundingRepository.findById(fundingId)).thenReturn(Optional.of(funding));

        // when & then
        assertThatThrownBy(() -> fundingExpireUseCase.expireFunding(fundingId))
                .isInstanceOf(FundingException.class)
                .extracting(e -> ((FundingException) e).getErrorCode())
                .isEqualTo(FundingErrorCode.ALREADY_TERMINATED);

        verify(fundingRepository, times(1)).findById(fundingId);
        verify(eventPublisher, never()).publish(any());
    }

    // ===== expireExpiredFundings (배치 만료) 테스트 =====

    @Test
    @DisplayName("expireExpiredFundings - 만료된 펀딩들 일괄 처리 성공")
    void expireExpiredFundings_success_with_multiple_fundings() {
        // given
        FundingWishlistItem item1 = createTestWishlistItem();
        FundingWishlistItem item2 = createTestWishlistItem();
        FundingWishlistItem item3 = createTestWishlistItem();

        Funding funding1 = Funding.startFunding(item1, 10000);
        Funding funding2 = Funding.startFunding(item2, 20000);
        Funding funding3 = Funding.startFunding(item3, 10000); // ACHIEVED로 만들기 위해
        funding3.contribute(40000); // 추가로 40,000원 → 목표 달성

        // 모두 endAt을 과거로 설정
        setEndAtToPast(funding1);
        setEndAtToPast(funding2);
        setEndAtToPast(funding3);

        List<Funding> expiredFundings = List.of(funding1, funding2, funding3);

        when(fundingRepository.findByEndAtBeforeAndStatusIn(
                any(LocalDateTime.class),
                eq(List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED))
        )).thenReturn(expiredFundings);

        // when
        List<FundingCompleteResponseDto> results = fundingExpireUseCase.expireExpiredFundings();

        // then
        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(FundingCompleteResponseDto::status)
                .containsOnly(FundingStatus.EXPIRED);

        verify(fundingRepository, times(1))
                .findByEndAtBeforeAndStatusIn(any(LocalDateTime.class), any());
        verify(eventPublisher, times(3)).publish(any()); // 각 펀딩마다 이벤트 발행
    }

    @Test
    @DisplayName("expireExpiredFundings - 만료된 펀딩이 없으면 빈 리스트 반환")
    void expireExpiredFundings_return_empty_when_no_expired_fundings() {
        // given
        when(fundingRepository.findByEndAtBeforeAndStatusIn(
                any(LocalDateTime.class),
                any()
        )).thenReturn(List.of());

        // when
        List<FundingCompleteResponseDto> results = fundingExpireUseCase.expireExpiredFundings();

        // then
        assertThat(results).isEmpty();

        verify(fundingRepository, times(1))
                .findByEndAtBeforeAndStatusIn(any(LocalDateTime.class), any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("expireExpiredFundings - IN_PROGRESS와 ACHIEVED 상태만 조회")
    void expireExpiredFundings_query_only_in_progress_and_achieved() {
        // given
        when(fundingRepository.findByEndAtBeforeAndStatusIn(
                any(LocalDateTime.class),
                eq(List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED))
        )).thenReturn(List.of());

        // when
        fundingExpireUseCase.expireExpiredFundings();

        // then
        verify(fundingRepository, times(1))
                .findByEndAtBeforeAndStatusIn(
                        any(LocalDateTime.class),
                        eq(List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED))
                );
    }

    @Test
    @DisplayName("expireExpiredFundings - 각 펀딩마다 FundingExpiredEvent 발행")
    void expireExpiredFundings_publish_event_for_each_funding() {
        // given
        FundingWishlistItem item1 = createTestWishlistItem();
        FundingWishlistItem item2 = createTestWishlistItem();

        Funding funding1 = Funding.startFunding(item1, 10000);
        Funding funding2 = Funding.startFunding(item2, 20000);

        setEndAtToPast(funding1);
        setEndAtToPast(funding2);

        List<Funding> expiredFundings = List.of(funding1, funding2);

        when(fundingRepository.findByEndAtBeforeAndStatusIn(
                any(LocalDateTime.class),
                any()
        )).thenReturn(expiredFundings);

        // when
        fundingExpireUseCase.expireExpiredFundings();

        // then
        verify(eventPublisher, times(2)).publish(any()); // 2번 호출
    }
}

