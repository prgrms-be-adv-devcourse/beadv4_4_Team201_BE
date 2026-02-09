package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.inbound.dto.FundingResponseDto;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FundingFacadeTest {

    @InjectMocks
    private FundingFacade fundingFacade;

    @Mock
    private FundingCreateUseCase fundingCreateUseCase;

    @Mock
    private FundingContributeUseCase fundingContributeUseCase;

    @Test
    @DisplayName("contributeFunding 성공: 펀딩 기여")
    void contributeFunding_Success() {
        // given
        Long fundingId = 100L;
        Long participantId = 2L;
        Integer amount = 5000;

        // when
        fundingFacade.contributeFunding(fundingId, participantId, amount);

        // then
        verify(fundingContributeUseCase).contribute(fundingId, participantId, amount);
    }
}
