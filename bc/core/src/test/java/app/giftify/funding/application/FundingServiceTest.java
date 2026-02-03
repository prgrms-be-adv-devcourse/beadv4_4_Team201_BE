package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.shared.domain.vo.FundingSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FundingServiceTest {

    @InjectMocks
    private FundingService fundingService;

    @Mock
    private FundingRepository fundingRepository;

    @Test
    @DisplayName("getSnapshot 성공: 활성화된 펀딩이 존재할 때")
    void getSnapshot_Success() {
        // given
        Long wishlistItemId = 1L;
        Long fundingId = 100L;
        FundingStatus status = FundingStatus.IN_PROGRESS;
        Funding funding = mock(Funding.class);
        given(funding.getId()).willReturn(fundingId);
        given(funding.getStatus()).willReturn(status);

        given(fundingRepository.findByWishlistItemIdAndStatus(wishlistItemId, status))
                .willReturn(Optional.of(funding));

        // when
        Optional<FundingSnapshot> result = fundingService.getSnapshot(wishlistItemId, status);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().fundingId()).isEqualTo(fundingId);
        assertThat(result.get().status()).isEqualTo(status);
    }

    @Test
    @DisplayName("getSnapshot 실패: 활성화된 펀딩이 없을 때")
    void getSnapshot_Fail_NoActiveFunding() {
        // given
        Long wishlistItemId = 1L;
        FundingStatus status = FundingStatus.IN_PROGRESS;

        given(fundingRepository.findByWishlistItemIdAndStatus(wishlistItemId, status))
                .willReturn(Optional.empty());

        // when
        Optional<FundingSnapshot> result = fundingService.getSnapshot(wishlistItemId, status);

        // then
        assertThat(result).isEmpty();
    }
}
