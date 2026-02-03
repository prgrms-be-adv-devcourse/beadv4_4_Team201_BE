package app.giftify.funding.adpater.inbound.web;

import app.giftify.funding.application.FundingService;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.shared.domain.vo.FundingSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InternalFundingControllerTest {

    @InjectMocks
    private InternalFundingController internalFundingController;

    @Mock
    private FundingService fundingService;

    @Test
    @DisplayName("getFundingSnapshotByWishlistItem 성공: 펀딩 스냅샷 반환")
    void getFundingSnapshotByWishlistItem_Success() {
        // given
        Long wishlistItemId = 1L;
        FundingStatus status = FundingStatus.IN_PROGRESS;
        FundingSnapshot snapshot = new FundingSnapshot(100L, status);
        given(fundingService.getSnapshot(wishlistItemId, status)).willReturn(Optional.of(snapshot));

        // when
        ResponseEntity<FundingSnapshot> response = internalFundingController.getFundingSnapshotByWishlistItemAndStatus(wishlistItemId, status);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(snapshot);
    }

    @Test
    @DisplayName("getFundingSnapshotByWishlistItem 실패: 펀딩 스냅샷 없음")
    void getFundingSnapshotByWishlistItem_Fail_NotFound() {
        // given
        Long wishlistItemId = 1L;
        FundingStatus status = FundingStatus.IN_PROGRESS;
        given(fundingService.getSnapshot(wishlistItemId, status)).willReturn(Optional.empty());

        // when
        ResponseEntity<FundingSnapshot> response = internalFundingController.getFundingSnapshotByWishlistItemAndStatus(wishlistItemId, status);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}
